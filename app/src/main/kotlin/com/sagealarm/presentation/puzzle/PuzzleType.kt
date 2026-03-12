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
    CAPTCHA(
        displayName = "문자 인식",
        description = "왜곡된 문자를 보고 대소문자를 구분하여 그대로 입력하세요.",
        instruction = "문자를 정확히 입력하세요 (대소문자 구분)",
    ),
}
