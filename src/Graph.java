import java.io.Serializable;
import java.util.*;

public class Graph<T> implements Serializable {

    // We use Hashmap to store the edges in the graph
    public Map<T, SortedSet<T> > map = new HashMap<>();
    public Map<T, Integer > color = new HashMap<>();

    public void initColors()
    {
        this.color.keySet().forEach(key -> this.color.put(key, -1));
    }

    // This function adds a new vertex to the graph
    public void addVertex(T s)
    {
        map.put(s, new TreeSet<>());
        color.put(s, -1);
    }

    public void changeColor(T s, Integer newColor)
    {
        color.put(s, newColor);
    }

    // This function adds the edge
    // between source to destination
    public void addEdge(T source,
                        T destination,
                        boolean bidirectional)
    {

        if (!map.containsKey(source))
            addVertex(source);

        if (!map.containsKey(destination))
            addVertex(destination);

        map.get(source).add(destination);
        if (bidirectional) {
            map.get(destination).add(source);
        }
    }

    // This function gives the count of vertices
    public int getVertexCount()
    {
        return map.keySet().size();
    }

    // This function gives the count of edges
    public void getEdgesCount(boolean bidirection)
    {
        int count = 0;
        for (T v : map.keySet()) {
            count += map.get(v).size();
        }
        if (bidirection) {
            count = count / 2;
        }
        System.out.println("The graph has "
                + count
                + " edges.");
    }

    // This function gives whether
    // a vertex is present or not.
    public void hasVertex(T s)
    {
        if (map.containsKey(s)) {
            System.out.println("The graph contains "
                    + s + " as a vertex.");
        }
        else {
            System.out.println("The graph does not contain "
                    + s + " as a vertex.");
        }
    }

    // This function gives whether an edge is present or not.
    public boolean hasEdge(T s, T d)
    {
        return map.get(s).contains(d);
    }

    public SortedSet<T> getEdges(T vertex)  {
        return this.map.get(vertex);
    }

    // Prints the adjancency list of each vertex.
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (T v : map.keySet()) {
            builder.append(v.toString()).append(": ");
            for (T w : map.get(v)) {
                builder.append(w.toString()).append(" ");
            }
            builder.append("\n");
        }

        return (builder.toString());
    }

    public void createGraph(Integer size)
    {
        for(Integer i=0;i<size-1;i++){
            this.addVertex((T) i);
        }

        Integer source = size - 1, dest = 0;

        source = 0;
        dest = 3;
        this.addEdge((T)source, (T)dest,true);

        source = 0;
        dest = 7;
        this.addEdge((T)source, (T)dest,true);

        source = 0;
        dest = 0;
        this.addEdge((T)source, (T)dest,true);

        source = 1;
        dest = 4;
        this.addEdge((T)source, (T)dest,true);

        source = 1;
        dest = 1;
        this.addEdge((T)source, (T)dest,true);

        source = 1;
        dest = 7;
        this.addEdge((T)source, (T)dest,true);

        source = 2;
        dest = 2;
        this.addEdge((T)source, (T)dest,true);

        source = 3;
        dest = 3;
        this.addEdge((T)source, (T)dest,true);

        source = 3;
        dest = 5;
        this.addEdge((T)source, (T)dest,true);

        source = 3;
        dest = 7;
        this.addEdge((T)source, (T)dest,true);

        source = 4;
        dest = 4;
        this.addEdge((T)source, (T)dest,true);

        source = 4;
        dest = 9;
        this.addEdge((T)source, (T)dest,true);

        source = 5;
        dest = 5;
        this.addEdge((T)source, (T)dest,true);

        source = 5;
        dest = 8;
        this.addEdge((T)source, (T)dest,true);

        source = 6;
        dest = 6;
        this.addEdge((T)source, (T)dest,true);

        source = 6;
        dest = 8;
        this.addEdge((T)source, (T)dest,true);

        source = 7;
        dest = 7;
        this.addEdge((T)source, (T)dest,true);

        source = 7;
        dest = 9;
        this.addEdge((T)source, (T)dest,true);

        source = 9;
        dest = 9;
        this.addEdge((T)source, (T)dest,true);

        source = 8;
        dest = 8;
        this.addEdge((T)source, (T)dest,true);



        for(Integer i=0;i<size-1;i++){
            dest = i + 1;
            this.addEdge((T) i,(T)dest,true);
        }
    }

    public void createRandomGraph(Integer size, Integer nrEdges)
    {
        for(Integer i=0;i<size;i++){
            this.addVertex((T) i);
            this.addEdge((T)i, (T)i, true);
        }
        Integer source, dest;
        for(Integer i=0;i<size-1;i++){
            dest = i + 1;
            this.addEdge((T) i,(T)dest,true);
        }

        //nrEdges is given as the number of edges we want to add after adding
        //edges between every vertex

        while (nrEdges != 0)
        {
            source = new Random().nextInt(size);
            dest = new Random().nextInt(size);

            if(source != dest) {
                this.addEdge((T) source, (T) dest, true);
                nrEdges--;
            }
        }
    }
}
