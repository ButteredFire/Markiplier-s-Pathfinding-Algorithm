package com.example.interactivetracker;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class ContentNode {
    protected int id, parent_id;
    protected String nodeName;

    protected List<ContentNode> subordinates;

    public ContentNode(String id, String nodeName, String parent_id) {
        this.id = Integer.parseInt(id);
        this.nodeName = nodeName;
        //this.dispName = dispName;
        this.parent_id = Integer.parseInt(parent_id);
    }
}

public class DecisionTree {
    public final String callSign = "DecisionTree";

    private Map<Integer, ContentNode> contentNodeMap = new HashMap<>();
    public ContentNode rootNode;

    private List<ContentNode> getSubordinates(int parent_id) {
        List<ContentNode> subords = new ArrayList<ContentNode>();
        for(ContentNode node : contentNodeMap.values()) {
            if(node.parent_id == parent_id) { subords.add(node); }
        }

        return subords;
    }

    public void createMap(String[] str_nodes) {
        ContentNode node = null;
        for(String str_node : str_nodes) {
            String[] values = str_node.split("-");
            node = new ContentNode(values[0], values[1], values[2]);
            contentNodeMap.put(node.id, node);

            if(node.parent_id == 0) { rootNode = node; }
        }

    }

    public void build(ContentNode rootNode) {
        ContentNode node = rootNode;
        List<ContentNode> subords = getSubordinates(node.id);
        node.subordinates = subords;
        if(subords.size() == 0) { return; }
        for(ContentNode subord : subords) {
            build(subord);
        }
    }

    public void printTree(ContentNode rootNode, int fromGeneration) {
            for(int i=0; i < fromGeneration; i++) {
                Log.i(callSign, "[space]");
            }
            Log.i(callSign, rootNode.nodeName);

            List<ContentNode> subords = rootNode.subordinates;
            for(ContentNode node : subords) {
                printTree(node, fromGeneration + 1);
            }

    }

    public JSONObject getResults() {
        Gson gson = new Gson();
        String json = gson.toJson(contentNodeMap);
        JSONObject results = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(json);
            results = jsonObject.getJSONObject(String.valueOf(1));
            Log.i(callSign, jsonObject.getJSONObject(String.valueOf(1)).toString());

        } catch (JSONException e) {
            Log.e(callSign, "JSONException occurred!");
            e.printStackTrace();
        }

        return results;
    }

}
