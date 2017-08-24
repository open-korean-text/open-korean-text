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

package org.openkoreantext.processor.util

import java.io.InputStream
import java.util
import java.util.zip.GZIPInputStream

import org.openkoreantext.processor.util.KoreanConjugation._
import org.openkoreantext.processor.util.KoreanPos._

import scala.collection.JavaConverters._
import scala.io.Source

/**
  * Provides a singleton Korean dictionary
  */
object KoreanDictionaryProvider {
  private[this] def readStreamByLine(stream: InputStream, filename: String): Iterator[String] = {
    require(stream != null, "Resource not loaded: " + filename)
    Source.fromInputStream(stream)(io.Codec("UTF-8"))
      .getLines()
      .map(_.trim)
      .filter(_.length > 0)
  }

  private[this] def readWordFreqs(filename: String): util.HashMap[CharSequence, Float] = {
    var freqMap: util.HashMap[CharSequence, Float] =
      new java.util.HashMap[CharSequence, Float]

    readFileByLineFromResources(filename).foreach {
      line =>
        if (line.contains("\t")) {
          val data = line.split("\t")
          freqMap.put(data(0), data(1).slice(0, 6).toFloat)
        }
    }
    freqMap
  }

  private[this] def readWordMap(filename: String): Map[String, String] = {
    readFileByLineFromResources(filename).filter {
      case line: String => line.contains(" ")
    }.map {
      case line =>
        val data = line.split(" ")
        (data(0), data(1))
    }.toMap
  }


  protected[processor] def readWordsAsSeq(filename: String): Seq[String] =
    readFileByLineFromResources(filename).toSeq


  protected[processor] def readWordsAsSet(filenames: String*): Set[String] = {
    filenames.foldLeft(Set[String]()) {
      case (output: Set[String], filename: String) =>
        output.union(
          readFileByLineFromResources(filename).toSet
        )
    }
  }

  protected[processor] def readWords(filenames: String*): CharArraySet = {
    val set = newCharArraySet
    filenames.foreach(
      filename => readFileByLineFromResources(filename).foreach(set.add)
    )
    set
  }

  protected[processor] def readFileByLineFromResources(filename: String): Iterator[String] = {
    readStreamByLine(
      if (filename.endsWith(".gz")) {
        new GZIPInputStream(getClass.getResourceAsStream(filename))
      } else {
        getClass.getResourceAsStream(filename)
      }
      , filename
    )
  }

  protected[processor] def newCharArraySet: CharArraySet = {
    new CharArraySet(10000, false)
  }

  lazy val koreanEntityFreq: util.HashMap[CharSequence, Float] =
    readWordFreqs("freq/entity-freq.txt.gz")

  def addWordsToDictionary(pos: KoreanPos, words: Seq[String]): Unit = {
    koreanDictionary.get(pos).addAll(words.asJava)
  }

  val koreanDictionary: util.HashMap[KoreanPos, CharArraySet] = {
    val map: util.HashMap[KoreanPos, CharArraySet] =
      new java.util.HashMap[KoreanPos, CharArraySet]

    map.put(Noun, readWords(
      "noun/nouns.txt", "noun/entities.txt", "noun/spam.txt",
      "noun/names.txt", "noun/twitter.txt", "noun/lol.txt",
      "noun/slangs.txt", "noun/company_names.txt",
      "noun/foreign.txt", "noun/geolocations.txt", "noun/profane.txt",
      "substantives/given_names.txt", "noun/kpop.txt", "noun/bible.txt",
      "noun/pokemon.txt", "noun/congress.txt", "noun/wikipedia_title_nouns.txt",
      "noun/brand.txt", "noun/fashion.txt", "noun/neologism.txt"
    ))
    map.put(Verb, conjugatePredicatesToCharArraySet(readWordsAsSet("verb/verb.txt")))
    map.put(Adjective, conjugatePredicatesToCharArraySet(readWordsAsSet("adjective/adjective.txt"), isAdjective = true))
    map.put(Adverb, readWords("adverb/adverb.txt"))
    map.put(Determiner, readWords("auxiliary/determiner.txt"))
    map.put(Exclamation, readWords("auxiliary/exclamation.txt"))
    map.put(Josa, readWords("josa/josa.txt"))
    map.put(Eomi, readWords("verb/eomi.txt"))
    map.put(PreEomi, readWords("verb/pre_eomi.txt"))
    map.put(Conjunction, readWords("auxiliary/conjunctions.txt"))
    map.put(Modifier, readWords("substantives/modifier.txt"))
    map.put(VerbPrefix, readWords("verb/verb_prefix.txt"))
    map.put(Suffix, readWords("substantives/suffix.txt"))
    map
  }

  lazy val spamNouns = readWords("noun/spam.txt", "noun/profane.txt")

  val properNouns: CharArraySet = readWords("noun/entities.txt",
    "noun/names.txt", "noun/twitter.txt", "noun/lol.txt", "noun/company_names.txt",
    "noun/foreign.txt", "noun/geolocations.txt",
    "substantives/given_names.txt", "noun/kpop.txt", "noun/bible.txt",
    "noun/pokemon.txt", "noun/congress.txt", "noun/wikipedia_title_nouns.txt",
    "noun/brand.txt", "noun/fashion.txt", "noun/neologism.txt")

  lazy val nameDictionary = Map(
    'family_name -> readWords("substantives/family_names.txt"),
    'given_name -> readWords("substantives/given_names.txt"),
    'full_name -> readWords("noun/kpop.txt", "noun/foreign.txt", "noun/names.txt")
  )

  lazy val typoDictionaryByLength = readWordMap("typos/typos.txt").groupBy {
    case (key: String, value: String) => key.length
  }

  lazy val predicateStems = {
    def getConjugationMap(words: Set[String], isAdjective: Boolean): Map[String, String] = {
      words.flatMap {
        word: String =>
          conjugatePredicated(Set(word), isAdjective).map {
            conjugated => (conjugated.toString, word + "다")
          }
      }.toMap
    }

    Map(
      Verb -> getConjugationMap(readWordsAsSet("verb/verb.txt"), isAdjective = false),
      Adjective -> getConjugationMap(readWordsAsSet("adjective/adjective.txt"), isAdjective = true)
    )
  }
}

