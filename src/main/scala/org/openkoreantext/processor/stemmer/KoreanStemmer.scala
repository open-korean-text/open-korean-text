package org.openkoreantext.processor.stemmer

import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken
import org.openkoreantext.processor.util.KoreanDictionaryProvider._
import org.openkoreantext.processor.util.KoreanPos._

/**
 * Stems Adjectives and Verbs: 새로운 스테밍을 추가했었다. -> 새롭다 + 스테밍 + 을 + 추가 + 하다
 */
object KoreanStemmer {
  private val Endings = Set(Eomi, PreEomi)
  private val Predicates = Set(Verb, Adjective)

  private val EndingsForNouns = Set("하다", "되다", "없다")

  /**
   * Removes Ending tokens recovering the root form of predicates
   *
   * @param tokens A sequence of tokens
   * @return A sequence of collapsed Korean tokens
   */
  def stem(tokens: Seq[KoreanToken]): Seq[KoreanToken] = {
    if (!tokens.exists(t => t.pos == Verb || t.pos == Adjective)) {
      return tokens
    }

    tokens.foldLeft(List[KoreanToken]()) {
      case (l: List[KoreanToken], token: KoreanToken) if l.nonEmpty && Endings.contains(token.pos) =>
        if (Predicates.contains(l.head.pos)) {
          val prevToken = l.head
          KoreanToken(
            prevToken.text + token.text,
            prevToken.pos, prevToken.offset, prevToken.length + token.length,
            stem = prevToken.stem,
            unknown = prevToken.unknown
          ) :: l.tail
        } else {
          token :: l
        }
      case (l: List[KoreanToken], token: KoreanToken) if Predicates.contains(token.pos) =>
        KoreanToken(
          token.text,
          token.pos, token.offset, token.length,
          stem = Some(predicateStems(token.pos)(token.text)),
          unknown = token.unknown
        ) :: l
      case (l: List[KoreanToken], token: KoreanToken) => token :: l
    }.reverse
  }
}
