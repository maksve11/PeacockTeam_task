package org.maksvell;

import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        if (args.length < 1) {
            System.out.println("Write a valid filepath");
            return;
        }

        String filePath = args[0];
        List<List<String>> lines = readLinesFromFile(filePath);
        if (lines.isEmpty()) {
            System.out.println("File is empty");
            return;
        }

        List<List<String>> groups = findLineGroups(lines);

        // Сортировка группы по размерам
        Collections.sort(groups, (g1, g2) -> Integer.compare(g2.size(), g1.size()));

        // Выводим группы в файл
        String outputDirectory = new File(filePath).getParent();
        String outputFilePath = outputDirectory + "/output.txt";
        writeGroupsToFile(outputFilePath, groups);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Time of completion: " + executionTime + " milliseconds");

        System.out.println("Count of groups with more than 1 element: " + groups.size());
    }

    private static List<List<String>> readLinesFromFile(String filePath) {
        List<List<String>> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                List<String> lineValues = new ArrayList<>();
                for (String value : values) {
                    if (!value.isEmpty()) {
                        lineValues.add(value);
                    }
                }
                lines.add(lineValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    private static List<List<String>> findLineGroups(List<List<String>> lines) {
        if (lines == null)
            return Collections.emptyList();

        List<List<String>> linesGroups = new ArrayList<>();

        if (lines.size() < 2) {
            linesGroups.addAll(lines);
            return linesGroups;
        }

        Map<String, Integer> columnMap = new HashMap<>();
        List<Integer> unitedGroups = new ArrayList<>(Collections.nCopies(lines.size(), null));

        for (List<String> line : lines) {
            List<Integer> groupsWithSameElems = new ArrayList<>();
            List<String> newElements = new ArrayList<>();

            for (String element : line) {
                if (element.isEmpty())
                    continue;

                Integer groupNum = columnMap.get(element);
                if (groupNum != null) {
                    while (unitedGroups.size() > groupNum && unitedGroups.get(groupNum) != null) {
                        groupNum = unitedGroups.get(groupNum);
                    }
                    groupsWithSameElems.add(groupNum);
                } else {
                    newElements.add(element);
                }
            }

            int groupNumber;
            if (groupsWithSameElems.isEmpty()) {
                linesGroups.add(new ArrayList<>());
                groupNumber = linesGroups.size() - 1;
            } else {
                groupNumber = groupsWithSameElems.get(0);
            }

            for (String newElement : newElements) {
                columnMap.put(newElement, groupNumber);
            }

            for (int matchedGrNum : groupsWithSameElems) {
                if (matchedGrNum != groupNumber) {
                    unitedGroups.set(matchedGrNum, groupNumber);
                    linesGroups.get(groupNumber).addAll(linesGroups.get(matchedGrNum));
                    linesGroups.set(matchedGrNum, new ArrayList<>());
                }
            }

            List<String> currentGroup = new ArrayList<>(line);
            linesGroups.set(groupNumber, currentGroup);
        }

        linesGroups.removeAll(Collections.singleton(Collections.emptyList()));
        linesGroups.sort(Comparator.comparingInt(List::size));

        return linesGroups;
    }

    private static void writeGroupsToFile(String filePath, List<List<String>> groups) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < groups.size(); i++) {
                List<String> group = groups.get(i);

                if (group.size() < 2) {
                    continue;
                }

                writer.write("Group " + (i + 1) + System.lineSeparator());
                for (String line : group) {
                    writer.write(line + System.lineSeparator());
                }
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
