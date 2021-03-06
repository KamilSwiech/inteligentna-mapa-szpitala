package bean.pwr.imskamieskiego.path_search;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import bean.pwr.imskamieskiego.model.map.Edge;
import bean.pwr.imskamieskiego.model.map.EdgeFactory;
import bean.pwr.imskamieskiego.model.map.MapPoint;
import bean.pwr.imskamieskiego.model.map.MapPointFactory;
import bean.pwr.imskamieskiego.repository.IMapGraphRepository;

public class StubGraphMapRepository implements IMapGraphRepository {

    private List<MapPoint> nodes;
    private List<Edge> edges;

    public StubGraphMapRepository() {
        this.nodes = createNodes();
        this.edges = createEdges();
    }

    public List<MapPoint> createNodes(){

        //add regular nodes
        List<MapPoint> nodes = Arrays.asList(
                MapPointFactory.create(1, 1, 1, 0, -1, false),
                MapPointFactory.create(2, 1, 6, 0, -1, false),
                MapPointFactory.create(3, 4, 1, 0, -1, false),
                MapPointFactory.create(4, 4, 0, 0, -1, true),
                MapPointFactory.create(5, 4, 0, 1, -1, true),
                MapPointFactory.create(6, 4, 1, 1, -1, false),
                MapPointFactory.create(7, 2, 4, 1, -1, false),
                MapPointFactory.create(9, 1, 12, 0, -1, false),
                MapPointFactory.create(10, 2, 12, 1, -1, false),
                //add orphaned node
                MapPointFactory.create(8, 0, 0, 2, -1, false)
        );

        return nodes;
    }

    public List<Edge> createEdges(){
        List<Edge> edges = Arrays.asList(
                EdgeFactory.create(1, 2, 5),
                EdgeFactory.create(1, 3, 3),
                EdgeFactory.create(2, 3, 6),
                EdgeFactory.create(3, 4, 1),
                EdgeFactory.create(5, 6, 1),
                EdgeFactory.create(6, 7, 4),
                EdgeFactory.create(4, 5, 1),
                EdgeFactory.create(2, 9, 11),
                EdgeFactory.create(9, 10, 1),
                EdgeFactory.create(7, 10, 13)
        );
        return addOppositeDirection(edges);
    }


    private List<Edge> addOppositeDirection(List<Edge> edges){
        ArrayList<Edge> bidirectionalEdges = new ArrayList<>(2*edges.size());
        bidirectionalEdges.addAll(edges);
        for (Edge edge:edges) {
            bidirectionalEdges.add(
                    EdgeFactory.create(edge.getTo(),edge.getFrom(), edge.getLength()));
        }
        return bidirectionalEdges;
    }


    @Override
    public MapPoint getPointByID(int id) {
        for (MapPoint node:nodes) {
            if (node.getId()==id){
                return node;
            }
        }
        return null;
    }

    @Override
    public List<MapPoint> getPointByID(@NonNull List<Integer> id) throws NullPointerException {
        List<MapPoint> points = new ArrayList<>();
        for (Integer index:id) {
            MapPoint point = getPointByID(index);
            if(point!=null){
                points.add(point);
            }
        }
        return points;
    }

    @Override
    public List<Edge> getOutgoingEdges(int pointID) {
        List<Edge> foundEdges = new ArrayList<>();
        for (Edge edge:edges) {
            if (pointID == edge.getFrom()){
                foundEdges.add(edge);
            }
        }
        return foundEdges;
    }

    @Override
    public Map<Integer, List<Edge>> getOutgoingEdges(@NonNull List<Integer> pointID) throws NullPointerException {
        Map<Integer, List<Edge>> result = new Hashtable<>();

        for (Integer index:pointID) {
            List<Edge> list = getOutgoingEdges(index);
            if (!list.isEmpty()){
                result.put(index, list);
            }
        }
        return result;
    }
}
