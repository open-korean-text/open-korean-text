/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.openkoreantext.processor.OpenKoreanTextProcessor
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor.KoreanPhrase
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken

object ScalaOpenKoreanTextExample {
  def main(args: Array[String]) {
    val text = "한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ #한국어"

    // Normalize
    val normalized: CharSequence = OpenKoreanTextProcessor.normalize(text)
    println(normalized)
    // 한국어를 처리하는 예시입니다ㅋㅋ #한국어

    // Tokenize
    val tokens: Seq[KoreanToken] = OpenKoreanTextProcessor.tokenize(normalized)
    println(tokens)
    // List(한국어(Noun: 0, 3), 를(Josa: 3, 1),  (Space: 4, 1), 처리(Noun: 5, 2), 하는(Verb(하다): 7, 2),  (Space: 9, 1), 예시(Noun: 10, 2), 입니다(Adjective(이다): 12, 3), ㅋㅋㅋ(KoreanParticle: 15, 3),  (Space: 18, 1), #한국어(Hashtag: 19, 4))

    // Phrase extraction
    val phrases: Seq[KoreanPhrase] = OpenKoreanTextProcessor.extractPhrases(tokens, filterSpam = true, enableHashtags = true)
    println(phrases)
    // List(한국어(Noun: 0, 3), 처리(Noun: 5, 2), 처리하는 예시(Noun: 5, 7), 예시(Noun: 10, 2), #한국어(Hashtag: 18, 4))
  }
}
