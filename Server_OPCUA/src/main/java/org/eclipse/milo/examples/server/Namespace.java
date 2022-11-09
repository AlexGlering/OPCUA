/*
 * Copyright (c) 2021 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.examples.server;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import org.eclipse.milo.examples.server.ApiJsonRead.ApiCall;
import org.eclipse.milo.examples.server.ApiJsonRead.Device;
import org.eclipse.milo.examples.server.ApiJsonRead.Endpoints;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.dtd.DataTypeDictionaryManager;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.BaseEventTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

public class Namespace extends ManagedNamespaceWithLifecycle {

    public static final String NAMESPACE_URI = "urn:sdu:milo:ICPS";


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile Thread eventThread;
    private volatile boolean keepPostingEvents = true;

    private final Random random = new Random();

    private final DataTypeDictionaryManager dictionaryManager;

    private final SubscriptionModel subscriptionModel;

    public Namespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);

        subscriptionModel = new SubscriptionModel(server, this);
        dictionaryManager = new DataTypeDictionaryManager(getNodeContext(), NAMESPACE_URI);

        getLifecycleManager().addLifecycle(dictionaryManager);
        getLifecycleManager().addLifecycle(subscriptionModel);

        getLifecycleManager().addStartupTask(this::createAndAddNodes);

        getLifecycleManager().addLifecycle(new Lifecycle() {
            @Override
            public void startup() {
                startBogusEventNotifier();
            }
            @Override
            public void shutdown() {
                try {
                    keepPostingEvents = false;
                    eventThread.interrupt();
                    eventThread.join();
                } catch (InterruptedException ignored) {
                    // ignored
                }
            }
        });
    }

    public void hehe(){
        createAndAddNodes();
    }

    private void createAndAddNodes() {
        // Create a "HelloWorld" folder and add it to the node manager
        NodeId folderNodeId = newNodeId("ICPS");

        UaFolderNode folderNode = new UaFolderNode(
            getNodeContext(),
            folderNodeId,
            newQualifiedName("ICPS"),
            LocalizedText.english("ICPS")
        );

        getNodeManager().addNode(folderNode);

        // Make sure our new folder shows up under the server's Objects folder.
        folderNode.addReference(new Reference(
            folderNode.getNodeId(),
            Identifiers.Organizes,
            Identifiers.ObjectsFolder.expanded(),
            false
        ));

        // Add the rest of the nodes
        addVariableNodes(folderNode);
    }

    private void startBogusEventNotifier() {
        // Set the EventNotifier bit on Server Node for Events.
        UaNode serverNode = getServer()
            .getAddressSpaceManager()
            .getManagedNode(Identifiers.Server)
            .orElse(null);

        if (serverNode instanceof ServerTypeNode) {
            ((ServerTypeNode) serverNode).setEventNotifier(ubyte(1));

            // Post a bogus Event every couple seconds
            eventThread = new Thread(() -> {
                while (keepPostingEvents) {
                    try {
                        BaseEventTypeNode eventNode = getServer().getEventFactory().createEvent(
                            newNodeId(UUID.randomUUID()),
                            Identifiers.BaseEventType
                        );

                        eventNode.setBrowseName(new QualifiedName(1, "foo"));
                        eventNode.setDisplayName(LocalizedText.english("foo"));
                        eventNode.setEventId(ByteString.of(new byte[]{0, 1, 2, 3}));
                        eventNode.setEventType(Identifiers.BaseEventType);
                        eventNode.setSourceNode(serverNode.getNodeId());
                        eventNode.setSourceName(serverNode.getDisplayName().getText());
                        eventNode.setTime(DateTime.now());
                        eventNode.setReceiveTime(DateTime.NULL_VALUE);
                        eventNode.setMessage(LocalizedText.english("event message!"));
                        eventNode.setSeverity(ushort(2));

                        //noinspection UnstableApiUsage
                        getServer().getEventBus().post(eventNode);

                        eventNode.delete();
                    } catch (Throwable e) {
                        logger.error("Error creating EventNode: {}", e.getMessage(), e);
                    }

                    try {
                        //noinspection BusyWait
                        Thread.sleep(2_000);
                    } catch (InterruptedException ignored) {
                        // ignored
                    }
                }
            }, "bogus-event-poster");

            eventThread.start();
        }
    }

    private void addVariableNodes(UaFolderNode rootNode) {
        //addDevice(rootNode);
        Device[] devices = ApiCall.requestIdsConnected("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev");
        folderLoop(devices,rootNode);

    }

    private void addStatic(UaFolderNode rootNode) {
        //ref1
        UaFolderNode scalarTypesFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("ICPS/Static"),
            newQualifiedName("Static"),
            LocalizedText.english("Static")
        );

        getNodeManager().addNode(scalarTypesFolder);
        rootNode.addOrganizes(scalarTypesFolder);

        String name = "Int32";
        NodeId typeId = Identifiers.Int32;
        Variant variant = new Variant(uint(32));

        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("ICPS/Static/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setUserAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(typeId)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        node.setValue(new DataValue(variant));

        node.getFilterChain().addLast(new AttributeLoggingFilter(AttributeId.Value::equals));

        getNodeManager().addNode(node);
        scalarTypesFolder.addOrganizes(node);
    }

    private void addDynamic(UaFolderNode rootNode) {
        UaFolderNode dynamicFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("ICPS/Dynamic"),
            newQualifiedName("Dynamic"),
            LocalizedText.english("Dynamic")
        );

        getNodeManager().addNode(dynamicFolder);
        rootNode.addOrganizes(dynamicFolder);


        // Dynamic Int32
        {
            String name = "Int32";
            NodeId typeId = Identifiers.Int32;
            Variant variant = new Variant(0);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("ICPS/Dynamic/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(typeId)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

            node.setValue(new DataValue(variant));

            node.getFilterChain().addLast(
                new AttributeLoggingFilter(),
                AttributeFilters.getValue(
                    ctx ->
                        new DataValue(new Variant(random.nextInt(100)))
                )
            );

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }
    }


    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }


    //CREATING THE TREE STRUCTURE

    //adding folder to root
    void addDevice(UaFolderNode rootNode, String deviceName) {
        //String deviceName = "TEST DEVICE";

        //create folder note
        UaFolderNode deviceFolder = new UaFolderNode(
                getNodeContext(),
                //build tree structure
                newNodeId("ICPS/"+deviceName),
                //give name
                newQualifiedName(""+deviceName),
                LocalizedText.english(""+deviceName)
        );
        //adding folder to node manager
        getNodeManager().addNode(deviceFolder);
        //adding folder to root
        rootNode.addOrganizes(deviceFolder);
    }

    private void addLogicalDevice(UaFolderNode deviceFolder){
        String deviceName = "";
        String logicalDeviceName = "TEST FOLDER";

        //create folder note
        UaFolderNode LogicalDeviceFolder = new UaFolderNode(
                getNodeContext(),
                //build tree structure
                newNodeId("ICPS/"+deviceName + "/"+ logicalDeviceName),
                //give name
                newQualifiedName(""+logicalDeviceName),
                LocalizedText.english(""+logicalDeviceName)
        );
        //adding folder to node manager
        getNodeManager().addNode(LogicalDeviceFolder);
        //adding folder to root
        deviceFolder.addOrganizes(LogicalDeviceFolder);
    }

    public void addNode(UaFolderNode logicalDeviceFolder, String nodeName,String logicalDeviceName, String deviceName, NodeId typeId){
        {
            //data node name
            //String logicalDeviceName="";
            //String deviceName = "";
            //String nodeName = "";
            //NodeId typeId = Identifiers.Int32; //data node datatype. Must be changed for each datanode

            //building node
            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("ICPS/" + deviceName + "/" + logicalDeviceName+ "/" + nodeName))
                    //setting server access level
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    //setting user access level
                    .setUserAccessLevel(AccessLevel.READ_WRITE)
                    //setting the browser name
                    .setBrowseName(newQualifiedName(nodeName))
                    //setting display name
                    .setDisplayName(LocalizedText.english(nodeName))
                    //setting the datatype
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();

            //adding node to node manager and device folder
            getNodeManager().addNode(node);
            logicalDeviceFolder.addOrganizes(node);
        }
    }
    public void folderLoop(Device[] devices, UaFolderNode root){
        String path;
        for(Device d: devices){
            path = "ICPS/"+d.specialID();
            UaFolderNode deviceFolder = new UaFolderNode(getNodeContext(),
                    newNodeId(path),
                    newQualifiedName(""+d.specialID()),
                    LocalizedText.english(""+d.specialID()));
            getNodeManager().addNode(deviceFolder);
            root.addOrganizes(deviceFolder);

            for (Endpoints n : d.getNicksfraekkeEndPoints()) {
                path+= "/"+n.getKey();
                UaFolderNode logicalFolder = new UaFolderNode(getNodeContext(),
                        newNodeId(path),
                        newQualifiedName(""+n.getKey()  ),
                        LocalizedText.english(""+n.getKey()));
                getNodeManager().addNode(logicalFolder);
                deviceFolder.addOrganizes(logicalFolder);


                for (JsonElement j : n.getEndpoints()) {
                    String asd = j.getAsJsonObject().get("key").toString();
                    asd = asd.substring(1,asd.length()-1);
                    path+= "/"+asd;
                    UaFolderNode endpoints = new UaFolderNode(getNodeContext(),
                            newNodeId(path),
                            newQualifiedName(asd  ),
                            LocalizedText.english(asd));
                    getNodeManager().addNode(endpoints);
                    logicalFolder.addOrganizes(endpoints);

                    for (Map.Entry<String, JsonElement> k : j.getAsJsonObject().entrySet()) {


                        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                                .setNodeId(newNodeId(endpoints.getNodeId() + k.getKey()))
                                .setAccessLevel(AccessLevel.READ_WRITE)
                                .setUserAccessLevel(getUserAccessLevel("n"))//FIND "access" here
                                .setBrowseName(newQualifiedName(k.getKey()))
                                .setDisplayName(LocalizedText.english(k.getKey()))
                                .setValue(new DataValue(new Variant(convertToDataType(k.getValue().getAsString()))))
                                .build();
                        endpoints.addComponent(node);
                        getNodeManager().addNode(node);
                        endpoints.addOrganizes(node);
                    }
                }
            }
        }
    }



    public ImmutableSet<AccessLevel> getUserAccessLevel(String s){
        switch (s){
            case "w" -> {
                return AccessLevel.READ_WRITE;
            }
            default -> {
                return AccessLevel.READ_ONLY;
            }
        }
    }


    private Object convertToDataType(String s){
        try{
            double output = Double.parseDouble(s);
            return output;
        }catch (NumberFormatException e){

        }
        try{
            int output = Integer.parseInt(s);
            return output;
        }catch (NumberFormatException e){

        }
        if(s == "true" || s == "false"){
            try{
                boolean output = Boolean.parseBoolean(s);
                return output;
            }catch (NumberFormatException e){

            }
        }

        return s;
    }

}
