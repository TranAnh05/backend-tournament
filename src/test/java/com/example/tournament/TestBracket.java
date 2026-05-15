package com.example.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestBracket {

    // 1. Class Mock giả lập Database
    static class MockMatch {
        int id;
        int bracketPosition;
        int roundLevel;
        MockMatch nextMatch;

        public MockMatch(int id, int bracketPosition, int roundLevel) {
            this.id = id;
            this.bracketPosition = bracketPosition;
            this.roundLevel = roundLevel;
        }

        @Override
        public String toString() {
            String nextId = (nextMatch != null) ? String.valueOf(nextMatch.id) : "NULL (Chung Kết)";
            return String.format("Match ID: %02d | Vòng: %d | Vị trí: %d | Trận tiếp theo (Next): %s",
                    id, roundLevel, bracketPosition, nextId);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ✨ BƯỚC 1: NHẬP SỐ ĐỘI MỖI TRẬN (CƠ SỐ CÂY)
        System.out.print("Nhập số đội thi đấu trong MỘT trận (Ví dụ: 2 (Cầu lông), 4 (Cờ liên quân), 8 (Bơi lội)): ");
        int teamsPerMatch = scanner.nextInt();

        if (teamsPerMatch < 2) {
            System.out.println("Số đội mỗi trận ít nhất phải là 2!");
            return;
        }

        // ✨ BƯỚC 2: NHẬP TỔNG SỐ ĐỘI THAM GIA
        System.out.print("Nhập TỔNG số lượng đội tham gia giải: ");
        int n = scanner.nextInt();

        // ---------------------------------------------------------
        // ✨ TRÁI TIM THUẬT TOÁN ĐA PHÂN (N-ARY TREE)
        // Thay vì Log cơ số 2, ta dùng Log cơ số K (teamsPerMatch)
        // ---------------------------------------------------------
        int nextPowerOfK = (int) Math.pow(teamsPerMatch, Math.ceil(Math.log(n) / Math.log(teamsPerMatch)));
        int totalRounds = (int) Math.round(Math.log(nextPowerOfK) / Math.log(teamsPerMatch));

        System.out.println("\n--- THÔNG TIN CẤU HÌNH TỰ ĐỘNG ---");
        System.out.println("- Số đội mỗi trận: " + teamsPerMatch);
        System.out.println("- Tổng số đội tham gia: " + n);
        System.out.println("- Số slot chuẩn (Lũy thừa của " + teamsPerMatch + "): " + nextPowerOfK);
        System.out.println("- Tổng số vòng đấu (Độ sâu cây): " + totalRounds + "\n");

        List<MockMatch> allMatches = new ArrayList<>();
        List<MockMatch> currentRoundMatches = new ArrayList<>();
        List<MockMatch> nextRoundMatches = new ArrayList<>();

        int fakeIdCounter = 1;

        // --- BƯỚC A: TẠO KHUNG TỪ CHUNG KẾT TRỞ XUỐNG ---
        for (int r = totalRounds; r >= 1; r--) {
            // Số trận trong vòng này = teamsPerMatch ^ (totalRounds - r)
            int matchesInRound = (int) Math.pow(teamsPerMatch, totalRounds - r);
            currentRoundMatches = new ArrayList<>();

            for (int i = 1; i <= matchesInRound; i++) {
                MockMatch match = new MockMatch(fakeIdCounter++, i, r);

                if (!nextRoundMatches.isEmpty()) {
                    // ✨ CÔNG THỨC CHỈ ĐỊNH CHA SIÊU VIỆT: (i - 1) / teamsPerMatch
                    // Đảm bảo cứ đúng K trận ở vòng này sẽ chụm lại trỏ về 1 trận ở vòng trên
                    MockMatch parentMatch = nextRoundMatches.get((i - 1) / teamsPerMatch);
                    match.nextMatch = parentMatch;
                }
                currentRoundMatches.add(match);
            }
            allMatches.addAll(currentRoundMatches);
            nextRoundMatches = currentRoundMatches;
        }

        // --- IN KẾT QUẢ ---
        System.out.println("========== TOÀN BỘ SƠ ĐỒ (" + allMatches.size() + " TRẬN) ==========");
        for (MockMatch m : allMatches) {
            System.out.println(m);
        }

        System.out.println("\n========== DANH SÁCH VÒNG 1 (LÁ CÂY) ==========");
        List<MockMatch> firstRoundMatches = nextRoundMatches;
        for (MockMatch m : firstRoundMatches) {
            System.out.println(m);
        }

        System.out.println("\n-> Để phân bổ " + n + " đội, hệ thống sẽ điền vào " + firstRoundMatches.size() + " trận Vòng 1.");

        scanner.close();
    }
}