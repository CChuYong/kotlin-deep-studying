package kr.chuyong.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() {
    val numberFlowBuilder = { (1..10).asFlow() }

    println("Case 1. Stream with transform")
    // Transform, filter 등 stream? 연산은 순서대로 실행한다.
    // 즉 다음과 같이 100ms를 기다리는 연산이 있으면, 다음 요소로 넘어가기 전에 100ms를 기다린다.
    numberFlowBuilder().transform {
        emit("I'm not multiplied! $it")
        emit("I'm multiplied! ${waitAndMultiply(it)}")
    }.collect {
        println(it)
    }

    println("Case 2. Stream with flatMapMerge")
    // 그럼 flatMap같이 리엑티브 스트림을 리턴으로 받는걸 사용하면
    // 다음 요소로 넘어가기 전에 100ms를 기다리는 연산이 있어도, 다음 요소로 넘어간다.
    numberFlowBuilder().flatMapMerge {
        flow {
            emit("I'm not multiplied! $it")
            emit("I'm multiplied! ${waitAndMultiply(it)}")
        }
    }.collect {
        println(it)
    }

    println("Case 3. Stream with flatMapConcat")
    //얜 결과가 위 Case 1과 같다. Concat 하기에, 플로우가 완료되고 나서 다음 플로우로 진입하는 듯 하다.
    numberFlowBuilder().flatMapConcat { number ->
        flow {
            emit("I'm not multiplied! $number")
            emit("I'm multiplied! ${waitAndMultiply(number)}")
        }
    }.collect {
        println(it)
    }

    println("Case 4. Stream with flatMapLatest")
    //Latest 즉 가장 최근의 플로우만을 받는다. 즉 새 element가 있으면 기존 flow를 잊어버리고 새 플로우를 탄다.
    // 마지막 element의 경우 새 플로우가 없으니 정상적으로 실행되고 끝난다.
    numberFlowBuilder().flatMapLatest {
        flow {
            emit("I'm not multiplied! $it")
            emit("I'm multiplied! ${waitAndMultiply(it)}")
        }
    }.collect {
        println(it)
    }
}

suspend fun waitAndMultiply(number: Int): Int {
    delay(100L)
    return number * 2
}
