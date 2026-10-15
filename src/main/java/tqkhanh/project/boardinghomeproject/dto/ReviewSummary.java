// ReviewSummary.java (distribution: phần tử 0 là số lượt 1 sao ... phần tử 4 là 5 sao)
package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record ReviewSummary(double average, long count, List<Long> distribution) {}