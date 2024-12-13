package program2;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        // Використовуємо JFileChooser для вибору директорії
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            // Створення ExecutorService з фіксованим пулом потоків
            ExecutorService executorService = Executors.newFixedThreadPool(4);

            // Запуск задачі пошуку
            FileSearchTask mainTask = new FileSearchTask(selectedDirectory, ".pdf", executorService);

            try {
                int totalCount = mainTask.call();
                JOptionPane.showMessageDialog(null, "Кількість PDF-файлів: " + totalCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            executorService.shutdown();
        } else {
            JOptionPane.showMessageDialog(null, "Директорію не було вибрано.");
        }
    }
}

class FileSearchTask implements Callable<Integer> {
    private final File directory;
    private final String fileExtension;
    private final ExecutorService executorService;

    public FileSearchTask(File directory, String fileExtension, ExecutorService executorService) {
        this.directory = directory;
        this.fileExtension = fileExtension;
        this.executorService = executorService;
    }

    @Override
    public Integer call() throws Exception {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files == null) return 0;

            List<Future<Integer>> futures = new ArrayList<>();

            for (File file : files) {
                if (file.isDirectory()) {
                    // Піддиректорії обробляються окремо
                    Callable<Integer> task = new FileSearchTask(file, fileExtension, executorService);
                    futures.add(executorService.submit(task));
                } else if (file.getName().endsWith(fileExtension)) {
                    // Знайдені файли додаються до підрахунку
                    futures.add(CompletableFuture.completedFuture(1));
                }
            }

            int totalCount = 0;
            for (Future<Integer> future : futures) {
                try {
                    totalCount += future.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            return totalCount;
        }

        return 0;
    }
}