package com.example.interactivetracker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class Results_Kotlin : AppCompatActivity() {
    fun hasConflicted(originalString: String, filters: ArrayList<String>): Boolean {
        for (filter in filters) {
            if (originalString.lowercase().contains(filter.lowercase())) {
                return true
            }
        }

        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_kotlin)

        var handler: Handler = Handler(Looper.getMainLooper())
        val callSign = "Results"

        // treeData_list = JSONObject(intent.getStringExtra("TREE_DATA_LIST").toString())
        //var treeData_name = JSONObject(intent.getStringExtra("TREE_DATA_NAME").toString())

        val rawData = intent.getStringArrayExtra("RAW_DATA")
        Log.i(callSign, Arrays.toString(rawData))
        //Name_tree.printTree(Name_tree.rootNode, 0);
        //val treeData_name = Name_tree.results

        val RV_dataStructure = findViewById<RecyclerView>(R.id.RV_dataStructureKotlin)

        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        RV_dataStructure.layoutManager = BuchheimWalkerLayoutManager(this, configuration)
            .apply { useMaxSize = true }

        RV_dataStructure.addItemDecoration(TreeEdgeDecoration())

        val graph = Graph()
        val identifier = JSONObject()

        Thread {
            Log.i(callSign, rawData!![0])

            //var incr_case = 0
            //val test_cases = 7

            val nodes = ArrayList<Node>()
            val nodeConflicts = ArrayList<String>()

            dataLoop@ for (node in rawData.iterator()) {
                Log.i(callSign, node)
                val nodeProperties = node.split("-").toTypedArray()
                Log.i(callSign, "iterating with: " + nodeProperties[1])
                /* nodeProperties[0] == nodeId
                 ..[1] == nodeName
                 ..[2] == managerId */

                val nodeId = nodeProperties[0]

                var nodeName = nodeProperties[1]
                if (hasConflicted(nodeName, nodeConflicts)) {
                    nodeName = "$nodeName ($nodeId)"
                } else {
                    nodeConflicts.add(nodeName)
                }

                var manager: String

                try {
                    manager = identifier.getString(nodeProperties[2])
                    identifier.put(nodeId, nodeName)
                    Log.i(callSign, identifier.toString())
                    nodes.add(Node(nodeName))

                    Log.i(callSign, "Parent of '$nodeName': '$manager'")

                    if(manager != "null") {
                        Log.i(callSign, "manager NodeRef: ")
                        Log.i(callSign, nodes[nodes.indexOf(Node(manager))].toString())
                        Log.i(callSign, "nodeName NodeRef: ")
                        Log.i(callSign, nodes[nodes.indexOf(Node(nodeName))].toString())

                        graph.addEdge(
                            nodes[nodes.indexOf(Node(manager))],
                            nodes[nodes.indexOf(Node(nodeName))]
                            //Node(manager),
                            //Node(nodeName)
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    identifier.put(nodeId, nodeName)
                    Log.i(callSign, identifier.toString())
                    nodes.add(Node(nodeName))
                }

            }

        }.start()



        val adapter = object : AbstractGraphAdapter<NodeViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.node, parent, false)
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                //holder = getNodeData(position).toString()
                holder.textView.text = getNodeData(position).toString()
            }

        }.apply {
            // 4.3 Submit the graph
            this.submitGraph(graph)
            RV_dataStructure.adapter = this
        }


    }
}
