package com.systemerr.sudokubackend;

import java.util.List;

public record SudokuBoards(List<SudokuBoard> grids, int results, String message) {
}