import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Main {
    static Integer NUMBER_OF_COLORS = 4;
    static Integer NUMBER_OF_THREADS = 3;
    public static void main(String[] args) throws InterruptedException {
        Graph<Integer> graph = new Graph<>();
        graph.createRandomGraph(10, 10);

        System.out.println(graph);
        long startTime = System.currentTimeMillis();
        if(GFG.canColor(graph, NUMBER_OF_COLORS, NUMBER_OF_THREADS))
            System.out.println("Can be colored with " + NUMBER_OF_COLORS + " colors");
        else
            System.out.println("Can't be colored with " + NUMBER_OF_COLORS + " colors");
        long endTime = System.currentTimeMillis();
        System.out.println("Regular implementation with " + graph.getVertexCount() +" and "+graph.getEdgesCount(true)+
                " edges: " + (endTime - startTime) + " ms");
    }
}

/*
0: 0 1 3 7
1: 0 1 2 4 7
2: 1 2 3
3: 0 2 3 4 5 7
4: 1 3 4 5 9
5: 3 4 5 6 8
6: 5 6 7 8
7: 0 1 3 6 7 8 9
8: 5 6 7 8 9
9: 4 7 8
 */

class WorkerThread implements Runnable {
    public Graph<Integer> graph;
    public List<Integer> vertexes;
    static List<ReentrantLock> mutexes;
    public Integer numberOfColors;
    public final int id;
    public AtomicInteger retVal;

    public WorkerThread(Graph<Integer> graph, List<Integer> vertexes, Integer numberOfColors, int id, AtomicInteger retVal) {
        this.graph = graph;
        this.vertexes = vertexes;
        this.numberOfColors = numberOfColors;
        this.id = id;
        this.retVal = retVal;
    }

    @Override
    public void run() {
        //System.out.println((id) + " " + vertexes);
        if (retVal.get() == 0)
            return;

        vertexes.forEach(v -> {
            var ref = new Object() {
                IntStream intStream = IntStream.range(1, numberOfColors + 1);
            };
            graph.map.get(v).forEach(neighbour -> mutexes.get(neighbour).lock());
            //System.out.print(v + ":");

            graph.map.get(v).forEach(neighbour -> {
                //System.out.print(neighbour + " ");
                if (graph.color.get(neighbour) != -1 && !Objects.equals(v, neighbour))
                {
                    ref.intStream = ref.intStream.filter(color -> color != graph.color.get(neighbour));
                }
            });

            var y = ref.intStream.boxed().toArray(Integer[]::new);
            //System.out.println("possible colors: " + Arrays.toString(y));
            graph.map.get(v).forEach(neighbour -> mutexes.get(neighbour).unlock());

            if(y.length != 0)
                graph.color.put(v, y[0]);
            else
                retVal.set(0);
        });
    }
}

class GFG {
    static boolean canColor(Graph<Integer> graph, int numberOfColors, int numberOfThreads) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);
        WorkerThread.mutexes = new ArrayList<>();
        graph.map.forEach((key, value) -> WorkerThread.mutexes.add(new ReentrantLock()));
        AtomicInteger retVal = new AtomicInteger(1);

        IntStream.range(0,numberOfThreads).forEach(id -> {
            WorkerThread w =
                    new WorkerThread(
                        graph,
                        new ArrayList<>(graph.map.keySet())
                            .subList(id * (graph.getVertexCount() / numberOfThreads), id * (graph.getVertexCount() / numberOfThreads) + (graph.getVertexCount() / numberOfThreads)),

                        numberOfColors,
                        id,
                        retVal
                    );
            threadPool.execute(w);
        });
        if (graph.getVertexCount() % numberOfThreads != 0) {
            WorkerThread w = new WorkerThread(graph,
                    new ArrayList<>(graph.map.keySet()).subList(numberOfThreads * (graph.getVertexCount() / numberOfThreads), graph.getVertexCount()),
                    numberOfColors,
                    numberOfThreads,
                    retVal);
            threadPool.execute(w);
        }
        threadPool.shutdown();
        // Wait for everything to finish.
        while (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("Waiting to finnish ...");
        }
        return retVal.get() == 1;
    }
}
