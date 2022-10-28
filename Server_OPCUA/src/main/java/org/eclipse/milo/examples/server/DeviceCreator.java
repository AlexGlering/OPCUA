package org.eclipse.milo.examples.server;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class DeviceCreator extends Namespace {

    DeviceCreator(OpcUaServer server) {
        super(server);
    }

    //adding folder to root
    void addDevice(UaFolderNode rootNode) {
        String deviceName = "";

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

    private void addSubFolder(UaFolderNode deviceFolder){
        String deviceName = "";
        String logicalDeviceName = "";

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

    public void addNode(UaFolderNode logicalDeviceFolder){
        {
            //data node name
            String logicalDeviceName="";
            String deviceName = "";
            String nodeName = "";
            NodeId typeId = Identifiers.Int32; //data node datatype

            //building node
            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("ICPS/" + deviceName + "/" + logicalDeviceName+ "/" + nodeName))
                    //setting server access level
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    //setting user access level
                    .setUserAccessLevel(AccessLevel.READ_ONLY)
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
}
