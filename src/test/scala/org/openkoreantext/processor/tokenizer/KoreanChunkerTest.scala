/*
 * Open Korean Text - Scala library to process Korean text
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

package org.openkoreantext.processor.tokenizer

import java.util.regex.Matcher

import com.twitter.Regex
import org.openkoreantext.processor.TestBase
import org.openkoreantext.processor.tokenizer.KoreanChunker._
import org.openkoreantext.processor.util.KoreanPos._

class KoreanChunkerTest extends TestBase {
  
  private def getPatternMatcher(pos: KoreanPos, text: String): Matcher = {
    return POS_PATTERNS(pos).matcher(text)
  }
  
  test("findAllPatterns should correctly find all patterns") {
    assert(
      findAllPatterns(getPatternMatcher(URL, "스팀(http://store.steampowered.com)에서 드디어 여름세일을 시작합니다."), URL).mkString("/")
        === "ChunkMatch(2,32,(http://store.steampowered.com,URL)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(Email, "만약 메일 주소가 하나 있고(예: hong@mail.com) 동시에 수백만명이 메일을 보낸다면 어떻게 될까?"), Email).mkString("/")
        === "ChunkMatch(19,32,hong@mail.com,Email)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(ScreenName, "트위터 아이디는 언제든지 변경이 가능합니다. @ironman을 @drstrange로 바꿀 수 있습니다."), ScreenName).mkString("/")
        === "ChunkMatch(34,45, @drstrange,ScreenName)/" +
            "ChunkMatch(24,33, @ironman,ScreenName)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(Hashtag, "구글에는 정말로 이쁜 자전거가 있다. #Google #이쁜자전거 #갖고싶다"), Hashtag).mkString("/")
        === "ChunkMatch(35,41, #갖고싶다,Hashtag)/" +
            "ChunkMatch(28,35, #이쁜자전거,Hashtag)/" +
            "ChunkMatch(20,28, #Google,Hashtag)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(CashTag, "주식정보 트윗 안내 : Twitter의 주식은 $twtr, Apple의 주식은 $appl 입니다."), CashTag).mkString("/")
        === "ChunkMatch(43,49, $appl,CashTag)/" +
            "ChunkMatch(25,31, $twtr,CashTag)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(Korean, "Hey! Can you speak Korean? 한국말! 오케이?"), Korean).mkString("/")
        === "ChunkMatch(32,35,오케이,Korean)/" +
            "ChunkMatch(27,30,한국말,Korean)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(KoreanParticle, "ㅋㅋ보다는 ㅎㅎ를 쓰라는데 무슨 차이인가요?"), KoreanParticle).mkString("/")
        === "ChunkMatch(6,8,ㅎㅎ,KoreanParticle)/" +
            "ChunkMatch(0,2,ㅋㅋ,KoreanParticle)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(Number, "6월 21일 개봉한 트랜스포머5:최후의 기사가 혹평 속에서도 박스오피스 1위를 달리고 있다."), Number).mkString("/")
        === "ChunkMatch(40,41,1,Number)/" +
            "ChunkMatch(16,17,5,Number)/" +
            "ChunkMatch(3,6,21일,Number)/" +
            "ChunkMatch(0,2,6월,Number)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(Alpha, "육회가 'six times', 곰탕이 'bear thang' 이라고? 아오! 정말 부끄러운 줄 알아랏!!"), Alpha).mkString("/")
        === "ChunkMatch(27,32,thang,Alpha)/" +
            "ChunkMatch(22,26,bear,Alpha)/" +
            "ChunkMatch(9,14,times,Alpha)/" +
            "ChunkMatch(5,8,six,Alpha)"
    )
    
    assert(
      findAllPatterns(getPatternMatcher(Punctuation, "비가 내리고... 음악이 흐르면... 난 당신을 생각해요~~"), Punctuation).mkString("/")
        === "ChunkMatch(31,33,~~,Punctuation)/" +
            "ChunkMatch(17,20,...,Punctuation)/" +
            "ChunkMatch(6,9,...,Punctuation)"
    )
  }

  test("getChunks should correctly split a string into Korean-sensitive chunks") {
    assert(
      getChunks("안녕? iphone6안녕? 세상아?").mkString("/")
        === "안녕/?/ /iphone/6/안녕/?/ /세상아/?"
    )

    assert(
      getChunks("This is an 한국어가 섞인 English tweet.").mkString("/")
        === "This/ /is/ /an/ /한국어가/ /섞인/ /English/ /tweet/."
    )

    assert(
      getChunks("이 日本것은 日本語Eng").mkString("/")
        === "이/ /日本/것은/ /日本語/Eng"
    )

    assert(
      getChunks("무효이며").mkString("/")
        === "무효이며"
    )

    assert(
      getChunks("#해쉬태그 이라는 것 #hash @hello 123 이런이런 #여자최애캐_5명으로_취향을_드러내자").mkString("/")
        === "#해쉬태그/ /이라는/ /것/ /#hash/ /@hello/ /123/ /이런이런/ /#여자최애캐_5명으로_취향을_드러내자"
    )
  }
  
  test("getChunksByPos should correctly extract chunks with a POS tag") {
    assert(
      getChunksByPos("openkoreantext.org에서 API를 테스트 할 수 있습니다.", URL).mkString("/")
        === "openkoreantext.org(URL: 0, 18)"
    )
    
    assert(
      getChunksByPos("메일 주소 mechanickim@openkoreantext.org로 문의주시거나", Email).mkString("/")
        === "mechanickim@openkoreantext.org(Email: 6, 30)"
    )
    
    assert(
      getChunksByPos("open-korean-text 프로젝트 마스터 @nlpenguin님께 메일주시면 됩니다. :-)", ScreenName).mkString("/")
        === "@nlpenguin(ScreenName: 26, 10)"
    )
    
    assert(
      getChunksByPos("해시태그는 이렇게 생겼습니다. #나는_해적왕이_될_사나이다", Hashtag).mkString("/")
        === "#나는_해적왕이_될_사나이다(Hashtag: 17, 15)"
    )
    
    assert(
      getChunksByPos("캐쉬태그는 주식정보 트윗할 때 사용합니다. $twtr", CashTag).mkString("/")
        === "$twtr(CashTag: 24, 5)"
    )
    
    assert(
      getChunksByPos("Black action solier 출두요~!", Korean).mkString("/")
        === "출두요(Korean: 20, 3)"
    )
    
    assert(
      getChunksByPos("Black action solier 출두요~! ㅋㅋ", KoreanParticle).mkString("/")
        === "ㅋㅋ(KoreanParticle: 26, 2)"
    )
    
    assert(
      getChunksByPos("최근 발매된 게임 '13일의 금요일'은 43,000원에 스팀에서 판매중입니다.", Number).mkString("/")
        === "13일(Number: 11, 3)/43,000원(Number: 22, 7)"
    )
    
    assert(
      getChunksByPos("드래곤볼 Z", Alpha).mkString("/")
        === "Z(Alpha: 5, 1)"
    )
    
    assert(
      getChunksByPos("나의 일기장 안에 모든 말을 다 꺼내어 줄 순 없지만... 사랑한다는 말 이에요.", Punctuation).mkString("/")
        === "...(Punctuation: 29, 3)/.(Punctuation: 44, 1)"
    )
  }

  test("getChunks should correctly extract numbers") {
    assert(
      getChunks("300위안짜리 밥").mkString("/")
        === "300위안/짜리/ /밥"
    )

    assert(
      getChunks("200달러와 300유로").mkString("/")
        === "200달러/와/ /300유로"
    )

    assert(
      getChunks("$200이나 한다").mkString("/")
        === "$200/이나/ /한다"
    )

    assert(
      getChunks("300옌이었다.").mkString("/")
        === "300옌/이었다/."
    )

    assert(
      getChunks("3,453,123,123원 3억3천만원").mkString("/")
        === "3,453,123,123원/ /3억/3천만원"
    )

    assert(
      getChunks("6/4 지방 선거").mkString("/")
        === "6/4/ /지방/ /선거"
    )

    assert(
      getChunks("6.4 지방 선거").mkString("/")
        === "6.4/ /지방/ /선거"
    )

    assert(
      getChunks("6-4 지방 선거").mkString("/")
        === "6-4/ /지방/ /선거"
    )

    assert(
      getChunks("6.25 전쟁").mkString("/")
        === "6.25/ /전쟁"
    )

    assert(
      getChunks("1998년 5월 28일").mkString("/")
        === "1998년/ /5월/ /28일"
    )

    assert(
      getChunks("62:45의 결과").mkString("/")
        === "62:45/의/ /결과"
    )

    assert(
      getChunks(" 여러 칸  띄어쓰기,   하나의 Space묶음으로 처리됩니다.").mkString("/")
        === " /여러/ /칸/  /띄어쓰기/,/   /하나의/ /Space/묶음으로/ /처리됩니다/."
    )
  }

  test("getChunkTokens should correctly find chunks with correct POS tags") {
    assert(
      chunk("한국어와 English와 1234와 pic.twitter.com " +
        "http://news.kukinews.com/article/view.asp?" +
        "page=1&gCode=soc&arcid=0008599913&code=41121111 " +
        "hohyonryu@twitter.com 갤럭시 S5").mkString("/")
        ===
        "한국어와(Korean: 0, 4)/ (Space: 4, 1)/English(Alpha: 5, 7)/와(Korean: 12, 1)/" +
          " (Space: 13, 1)/1234(Number: 14, 4)/와(Korean: 18, 1)/ (Space: 19, 1)/" +
          "pic.twitter.com(URL: 20, 15)/ (Space: 35, 1)/http://news.kukinews.com/" +
          "article/view.asp?page=1&gCode=soc&arcid=0008599913&code=41121111(URL: 36, 89)/" +
          " (Space: 125, 1)/hohyonryu@twitter.com(Email: 126, 21)/ (Space: 147, 1)/" +
          "갤럭시(Korean: 148, 3)/ (Space: 151, 1)/S(Alpha: 152, 1)/5(Number: 153, 1)"
    )

    assert(
      chunk("우와!!! 완전ㅋㅋㅋㅋ").mkString("/")
        === "우와(Korean: 0, 2)/!!!(Punctuation: 2, 3)/ (Space: 5, 1)/완전(Korean: 6, 2)/" +
        "ㅋㅋㅋㅋ(KoreanParticle: 8, 4)"
    )

    assert(
      chunk("@nlpenguin @edeng #korean_tokenizer_rocks 우하하").mkString("/")
        === "@nlpenguin(ScreenName: 0, 10)/ (Space: 10, 1)/@edeng(ScreenName: 11, 6)/" +
        " (Space: 17, 1)/#korean_tokenizer_rocks(Hashtag: 18, 23)/ (Space: 41, 1)/" +
        "우하하(Korean: 42, 3)"
    )
  }

  test("getChunkTokens should correctly detect Korean-specific punctuations.") {
    assert(
      chunk("중·고등학교에서…").mkString("/")
        === "중(Korean: 0, 1)/·(Punctuation: 1, 1)/고등학교에서(Korean: 2, 6)/…(Punctuation: 8, 1)"
    )
  }
}