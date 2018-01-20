/*
 * Open Korean Text - Scala library to process Korean text
 *
 * Copyright 2016 Twitter, Inc.
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

package org.openkoreantext.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.tokenizer.Sentence;
import scala.collection.Seq;

import static org.junit.Assert.assertEquals;

public class OpenKoreanProcessorJavaTest {
  @Test
  public void testNormalize() {
    assertEquals("힘들겠습니다 그래요ㅋㅋㅋ", OpenKoreanTextProcessorJava.normalize("힘들겟씀다 그래욬ㅋㅋㅋ"));
  }

  @Test
  public void testTokenize() {
    String text = "착한강아지상을 받은 루루";
    assertEquals(
        "List(착한(Adjective(착하다): 0, 2), 강아지(Noun: 2, 3), 상(Suffix: 5, 1), " +
            "을(Josa: 6, 1),  (Space: 7, 1), 받은(Verb(받다): 8, 2),  " +
            "(Space: 10, 1), 루루(Noun: 11, 2))",
        String.valueOf(OpenKoreanTextProcessorJava.tokenize(text))
    );

    text = "백여마리";
    assertEquals(
        "List(백여(Modifier: 0, 2), 마리(Noun: 2, 2))",
        String.valueOf(OpenKoreanTextProcessorJava.tokenize(text))
    );
  }

  @Test
  public void testTokensToJavaStringList() {
    String text = "착한강아지상을 받은 루루";
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals(
        "[착한, 강아지, 상, 을,  , 받은,  , 루루]",
        OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens, true).toString()
    );

    assertEquals(
        "[착한, 강아지, 상, 을, 받은, 루루]",
        OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens, false).toString()
    );
  }

  @Test
  public void testAddNounsToDictionary() {
    String text = "춍춍춍춍챵챵챵";

    // before
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals("[춍춍춍춍챵챵챵]", OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens).toString());

    // add nouns
    ArrayList<String> words = new ArrayList<>();
    words.add("춍춍");
    words.add("챵챵챵");
    OpenKoreanTextProcessorJava.addNounsToDictionary(words);

    // after
    tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals("[춍춍, 춍춍, 챵챵챵]", OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens).toString());
  }

  @Test
  public void testAddWordsToDictionary() {
    String text = "그라믄 당신 먼저 얼렁 가이소";

    // before
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals("[그, 라, 믄, 당신, 먼저, 얼렁, 가이소]", OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens).toString());
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).get(4).getPos(), KoreanPosJava.Noun);

    // add words
    ArrayList<String> conjs = new ArrayList<>();
    conjs.add("그라믄");
    OpenKoreanTextProcessorJava.addWordsToDictionary(KoreanPosJava.Conjunction, conjs);

    ArrayList<String> advs = new ArrayList<>();
    advs.add("얼렁");
    OpenKoreanTextProcessorJava.addWordsToDictionary(KoreanPosJava.Adverb, advs);

    // after
    tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals("[그라믄, 당신, 먼저, 얼렁, 가이소]", OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens).toString());
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).get(3).getPos(), KoreanPosJava.Adverb);
  }

  @Test
  public void testRemoveWordsFromDictionary() {
    String text = "평창올림픽에";

    ArrayList<String> nouns = new ArrayList<>();
    nouns.add("평창올림픽");
    OpenKoreanTextProcessorJava.addWordsToDictionary(KoreanPosJava.Noun, nouns);

    // before
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals("[평창올림픽, 에]", OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens).toString());
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).get(0).getPos(), KoreanPosJava.Noun);

    // remove word
    OpenKoreanTextProcessorJava.removeWordFromDictionary(KoreanPosJava.Noun, nouns);

    // after
    tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals("[평창, 올림픽, 에]", OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens).toString());
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).get(0).getPos(), KoreanPosJava.Noun);
  }

  @Test
  public void testTokensToJavaKoreanTokenList() throws Exception {
    String text = "착한강아지상을 받은 루루";
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals(
        "[착한(Adjective(착하다): 0, 2), 강아지(Noun: 2, 3), 상(Suffix: 5, 1), 을(Josa: 6, 1), " +
            " (Space: 7, 1), 받은(Verb(받다): 8, 2),  (Space: 10, 1), 루루(Noun: 11, 2)]",
        OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).toString()
    );

    assertEquals(
        "[착한(Adjective(착하다): 0, 2), 강아지(Noun: 2, 3), 상(Suffix: 5, 1), 을(Josa: 6, 1), " +
            "받은(Verb(받다): 8, 2), 루루(Noun: 11, 2)]",
        OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, false).toString()
    );

    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).get(0).getText(), "착한");
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).get(0).getStem(), "착하다");
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).get(0).getOffset(), 0);
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).get(0).getLength(), 2);
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).get(0).getPos(), KoreanPosJava.Adjective);
    assertEquals(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, true).get(0).isUnknown(), false);

    text = "백여마리";
    tokens = OpenKoreanTextProcessorJava.tokenize(text);
    assertEquals(
        "[백여(Modifier: 0, 2), 마리(Noun: 2, 2)]",
        OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens, false).toString()
    );
  }

  @Test
  public void testPhraseExtractor() {
    String text = "아름다운 트위터를 만들어 보자. 시발 #욕하지_말자";
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(text);

    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 트위터(Noun: 5, 3), #욕하지_말자(Hashtag: 21, 7)]",
        OpenKoreanTextProcessorJava.extractPhrases(tokens, true, true).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 트위터(Noun: 5, 3)]",
        OpenKoreanTextProcessorJava.extractPhrases(tokens, true, false).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 시발(Noun: 18, 2), 트위터(Noun: 5, 3), #욕하지_말자(Hashtag: 21, 7)]",
        OpenKoreanTextProcessorJava.extractPhrases(tokens, false, true).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 시발(Noun: 18, 2), 트위터(Noun: 5, 3)]",
        OpenKoreanTextProcessorJava.extractPhrases(tokens, false, false).toString()
    );
  }

  @Test
  public void testSentenceSplitter() {
    String text = "가을이다! 남자는 가을을 탄다...... 그렇지? 루루야! 버버리코트 사러 가자!!!!";
    List<Sentence> tokens = OpenKoreanTextProcessorJava.splitSentences(text);

    assertEquals(
        "[가을이다!(0,5), 남자는 가을을 탄다......(6,22), 그렇지?(23,27), 루루야!(28,32), 버버리코트 사러 가자!!!!(33,48)]",
        tokens.toString()
    );
  }

  @Test
  public void testDetokenizer() {
    List<String> words = Arrays.asList("늘", "평온", "하게", "누워", "있", "는", "루루");

    assertEquals(
        "늘 평온하게 누워있는 루루",
        OpenKoreanTextProcessorJava.detokenize(words)
    );
  }
}