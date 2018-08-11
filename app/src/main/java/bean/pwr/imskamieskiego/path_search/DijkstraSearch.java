package bean.pwr.imskamieskiego.path_search;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import bean.pwr.imskamieskiego.model.map.Edge;
import bean.pwr.imskamieskiego.model.map.MapPoint;
import bean.pwr.imskamieskiego.repository.MapRepository;

/**
 * Implementation of Dijkstra algorithm.
 * @see PathSearcher
 */
public class DijkstraSearch implements PathSearchAlgorithm {

    private MapPoint startPoint;
    private MapPoint endPoint;
    private MapRepository mapRepository;
    private List<MapPoint> trace;

    /**
     * Create instance of Dijkstra algorithm to search path between given points. Points can't have
     * the same ID (be the same node on graph)
     * @see PathSearcher
     * @param mapRepository repository with graph data
     * @param startPoint point from which the algorithm should start searching for the path
     * @param endPoint target point
     * @throws IllegalArgumentException throw when start and end point are the same node
     */
    public DijkstraSearch(MapRepository mapRepository, MapPoint startPoint, MapPoint endPoint) throws IllegalArgumentException {
        if (startPoint.getId()==endPoint.getId()){
            throw new IllegalArgumentException("End point and start point are the same points!");
        }
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.mapRepository = mapRepository;
        this.trace = new ArrayList<>();
    }

    /**
     * Start patch search between points given in constructor. Searching is a blocking operation
     * that can take a long time. Therefore, do not call it directly in the main thread.
     * @see PathSearcher
     */
    @Override
    public void startSearch() {
        //TODO depth fetch parameters should be chosen experimentally
        int initDepthFetch = 10;
        int depthFetch = 10;
        int startID = startPoint.getId();
        int endID = endPoint.getId();


        trace.clear();

        Map<Integer, Integer> nodeVisitHistory = new HashMap<>();
        Map<Integer, Integer> distancesFromStart = new HashMap<>();
        PriorityQueue<NodePriorityWrapper> pathToTravel = new PriorityQueue<>(100,
                (dist1, dist2) -> dist1.distance-dist2.distance);

        Map<Integer, List<Edge>> outgoingEdges = new HashMap<>(fetchEdges(startID, initDepthFetch));

        //We start from a point without outgoing edges. There is nowhere to go.
        if (outgoingEdges.isEmpty()){
            return;
        }

        nodeVisitHistory.put(startID, -1);
        distancesFromStart.put(startID, 0);
        pathToTravel.add(new NodePriorityWrapper(startID, distancesFromStart.get(startID)));

        while (!pathToTravel.isEmpty()){

            int from_id = pathToTravel.remove().id;

            if (from_id == endID){
                //The target has been achieved.
                generateTrace(nodeVisitHistory);
                return;
            }

            if (!outgoingEdges.containsKey(from_id)){
                outgoingEdges.putAll(fetchEdges(from_id, depthFetch));
            }

            for (Edge edge:outgoingEdges.get(from_id)) {

                int newBestDistance = edge.getLength() + distancesFromStart.get(from_id);
                int oldDistance = Integer.MAX_VALUE;
                if (distancesFromStart.containsKey(edge.getTo())){
                    oldDistance = distancesFromStart.get(edge.getTo());
                }

                if (newBestDistance < oldDistance){
                    distancesFromStart.put(edge.getTo(), newBestDistance);
                    nodeVisitHistory.put(edge.getTo(), from_id);
                    pathToTravel.add(new NodePriorityWrapper(edge.getTo(), newBestDistance));
                }
            }
        }
    }

    /**
     * Return the shortest path between points given in constructor. If searching didn't end or
     * can not find path, it returns empty;
     * @return found path
     */
    @Override
    @NonNull
    public List<MapPoint> getPath() {
        return trace;
    }


    private static class NodePriorityWrapper {
        int id;
        int distance;

        public NodePriorityWrapper(int id, int distance) {
            this.id = id;
            this.distance = distance;
        }
    }

    private Map<Integer, List<Edge>> fetchEdges(int nodeID, int depth) {
        if (depth < 0) depth = 0;
        List<Integer> toFetchList = new ArrayList<>();
        Map<Integer, List<Edge>> tmpFetchedEdges;
        Map<Integer, List<Edge>> fetchedEdges;

        toFetchList.add(nodeID);
        tmpFetchedEdges = mapRepository.getOutgoingEdges(toFetchList);
        fetchedEdges = tmpFetchedEdges;

        for (int i = 0; i < depth; i++) {
            toFetchList.clear();
            for (Integer id:tmpFetchedEdges.keySet()) {
                List<Edge> edges = tmpFetchedEdges.get(id);
                for (Edge edge : edges){
                    if (!fetchedEdges.containsKey(edge.getTo())) {
                        toFetchList.add(edge.getTo());
                    }
                }
            }
            tmpFetchedEdges = mapRepository.getOutgoingEdges(toFetchList);
            fetchedEdges.putAll(tmpFetchedEdges);

            if(tmpFetchedEdges.isEmpty()) break;
        }

        return fetchedEdges;
    }

    private void generateTrace(Map<Integer, Integer> nodeVisitHistory){
        int traceSize = nodeVisitHistory.size();
        if (traceSize == 0) return;
        if (!nodeVisitHistory.containsKey(endPoint.getId())) return;


        List<Integer> listOfVisits = new ArrayList<>();
        int previousID = endPoint.getId();

        while (previousID >= 0){
            listOfVisits.add(previousID);
            previousID = nodeVisitHistory.get(previousID);
        }

        List<MapPoint> unsortedMapPointList = mapRepository.getPointByID(listOfVisits);

        for (int i = listOfVisits.size()-1; i >= 0; i--) {
            int id = listOfVisits.get(i);
            for (MapPoint point:unsortedMapPointList) {
                if (point.getId() == id){
                    trace.add(point);
                    break;
                }
            }
        }
    }
}