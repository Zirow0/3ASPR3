package program1;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        // Вхідні дані від користувача
        System.out.print("Введіть кількість елементів масиву: ");
        int n = scanner.nextInt();
        System.out.print("Введіть нижню межу значення: ");
        int start = scanner.nextInt();
        System.out.print("Введіть верхню межу значення: ");
        int end = scanner.nextInt();

        scanner.close();

        // Створення масиву
        int[] array = generateRandomArray(n, start, end);

        System.out.println("Згенерований масив:");
        System.out.println(Arrays.toString(array));

        // Work Dealing
        long startTime = System.nanoTime();
        long sumWorkDividing = executeWorkDealing(array);
        long endTime = System.nanoTime();
        System.out.println("\nРезультат (Work Dealing): " + sumWorkDividing);
        System.out.println("Час виконання (Work Dealing): " + (endTime - startTime) / 1_000_000 + " мс");

        // Work Stealing
        startTime = System.nanoTime();
        long sumWorkStealing = executeWorkStealing(array);
        endTime = System.nanoTime();
        System.out.println("\nРезультат (Work Stealing): " + sumWorkStealing);
        System.out.println("Час виконання (Work Stealing): " + (endTime - startTime) / 1_000_000 + " мс");

    }

    private static int[] generateRandomArray(int n, int start, int end) {
        Random random = new Random();
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = random.nextInt(end - start + 1) + start;
        }
        return array;
    }

    // WorkDealing execution
    private static long executeWorkDealing(int[] array) throws InterruptedException, ExecutionException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int partSize = array.length / numThreads;
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int startIdx = i * partSize;
            final int endIdx = (i == numThreads - 1) ? array.length - 1 : (i + 1) * partSize;
            futures.add(executor.submit(() -> computeSum(array, startIdx, endIdx)));
        }

        long totalSum = 0;
        for (Future<Long> future : futures) {
            totalSum += future.get();
        }

        executor.shutdown();
        return totalSum;
    }

    private static long computeSum(int[] array, int start, int end) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += array[i] + array[i + 1];
        }
        return sum;
    }

    // WorkStealing execution
    private static long executeWorkStealing(int[] array) throws InterruptedException, ExecutionException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        PairSumTask task = new PairSumTask(array, 0, array.length - 1);
        long sum = forkJoinPool.invoke(task);
        forkJoinPool.shutdown();
        return sum;
    }

    // WorkStealing logic
    static class PairSumTask extends RecursiveTask<Long> {
        private final int[] array;
        private final int start, end;

        public PairSumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= 2) {
                return computeSum(array, start, end);
            }

            int mid = start + (end - start) / 2;
            PairSumTask leftTask = new PairSumTask(array, start, mid);
            PairSumTask rightTask = new PairSumTask(array, mid, end);

            leftTask.fork();
            rightTask.fork();
            long rightResult = rightTask.join();
            long leftResult = leftTask.join();

            return leftResult + rightResult;
        }
    }
}