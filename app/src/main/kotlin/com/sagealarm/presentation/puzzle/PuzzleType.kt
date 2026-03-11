package com.sagealarm.presentation.puzzle

enum class PuzzleType(
    val displayName: String,
    val description: String,
    val instruction: String,
) {
    NUMBER_ORDER(
        displayName = "숫자 순서",
        description = "화면에 흩어진 숫자 5개를 큰 수부터 차례대로 터치하세요.",
        instruction = "큰 수부터 차례대로 터치하세요",
    ),
}
