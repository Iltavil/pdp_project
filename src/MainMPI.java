import mpi.MPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class MainMPI {
    static Integer NUMBER_OF_COLORS = 4;

    public static void main(String[] args) {
        MPI.Init(args);

        int me = MPI.COMM_WORLD.Rank();
        int nrProcs = MPI.COMM_WORLD.Size();

        if(me == 0) {
            Integer retVal = 1;
            Graph<Integer> graph = new Graph<>();
            graph.createGraph(10);
            List<ReentrantLock> mutexes = new ArrayList<>();

            System.out.println(graph);
            graph.map.forEach((key, value) -> mutexes.add(new ReentrantLock()));

            int start = 0, finnish = 0;
            int len = graph.getVertexCount() / (nrProcs + 1);

            for (int i=1;i<nrProcs;i++)
            {
                start = finnish;
                finnish += len;

                if(i == nrProcs - 1)
                {
                   finnish = graph.getVertexCount();
                }

                var vertexes = new ArrayList<>(graph.map.keySet())
                        .subList(start, finnish);

                var vertexes2 = new ArrayList<>(vertexes);

                MPI.COMM_WORLD.Send(new Object[]{graph},0, 1, MPI.OBJECT, i, 0);
                MPI.COMM_WORLD.Send(new Object[]{vertexes2},0, 1, MPI.OBJECT, i, 0);
                MPI.COMM_WORLD.Send(new Object[]{mutexes},0, 1, MPI.OBJECT, i, 0);
                MPI.COMM_WORLD.Send(new int[]{NUMBER_OF_COLORS},0, 1, MPI.INT, i, 0);
                MPI.COMM_WORLD.Send(new int[]{retVal},0, 1, MPI.INT, i, 0);

            }

            int[] results = new int[nrProcs - 1];
            for (int i = 1; i < nrProcs; i++) {
                MPI.COMM_WORLD.Recv(results, i - 1, 1, MPI.INT, i, 0);
            }

            boolean retV = Arrays.stream(results).anyMatch(ret -> ret == 0);

            if (!retV)
                System.out.println("Can color with " + NUMBER_OF_COLORS + " colors" );
            else
                System.out.println("Can't color with " + NUMBER_OF_COLORS + " colors");
        }
        else
        {
            Object[] graph = new Object[2];
            Object[] vertexes = new Object[1];
            Object[] mutexes = new Object[1];
            int[] numberOfColors = new int[1];
            int[] retVal = new int[1];

            MPI.COMM_WORLD.Recv(graph,0, 1, MPI.OBJECT, 0, 0);
            MPI.COMM_WORLD.Recv(vertexes,0, 1, MPI.OBJECT, 0, 0);
            MPI.COMM_WORLD.Recv(mutexes,0, 1, MPI.OBJECT, 0, 0);
            MPI.COMM_WORLD.Recv(numberOfColors,0, 1, MPI.INT, 0, 0);
            MPI.COMM_WORLD.Recv(retVal,0, 1, MPI.INT, 0, 0);

            List<ReentrantLock> locks = (List<ReentrantLock>) mutexes[0];

            if (retVal[0] != 0)
            {
                Graph<Integer> graph1 = (Graph<Integer>)graph[0];
                ((List<Integer>)vertexes[0]).forEach(v -> {
                    var ref = new Object() {
                        IntStream intStream = IntStream.range(1, numberOfColors[0] + 1);
                    };
                    graph1.map.get(v).forEach(neighbour -> locks.get(neighbour).lock());
                    //System.out.print(v + ":");

                    graph1.map.get(v).forEach(neighbour -> {
                        //System.out.print(neighbour + " ");
                        if (graph1.color.get(neighbour) != -1 && !Objects.equals(v, neighbour))
                        {
                            ref.intStream = ref.intStream.filter(color -> color != graph1.color.get(neighbour));
                        }
                    });

                    var y = ref.intStream.boxed().toArray(Integer[]::new);
                    //System.out.println("possible colors: " + Arrays.toString(y));
                    graph1.map.get(v).forEach(neighbour -> locks.get(neighbour).unlock());

                    if(y.length != 0)
                        graph1.color.put(v, y[0]);
                    else
                        retVal[0] = 0;
                });
            }

            MPI.COMM_WORLD.Send(new int[]{retVal[0]}, 0, 1, MPI.INT, 0, 0);
        }
        MPI.Finalize();
    }
}
