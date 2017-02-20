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

package org.openkoreantext.processor.tools

import java.io.File
import java.io.FileOutputStream

import scala.io.Source

/**
 * Clean up resources by removing duplicates and sorting.
 */
object DeduplicateAndSortDictionaries extends Runnable  {

  private[this] def readWords(filename: String): Set[String] = {
    Source.fromFile(filename)(io.Codec("UTF-8"))
        .getLines()
        .map(_.trim)
        .filter(_.length > 0)
        .toSet
  }

  private val EXCEPTION_RESOURCES = Seq(
    "example_chunks.txt", "example_tweets.txt"
  )
  
  def work(file: File) {
    if (!file.exists) return;
    
    if (file.isDirectory) {
      val files = file.listFiles.toList
      for (file <- files) {
        work(file)
      }
    } else if (file.isFile) {
      val extPos = file.getName.lastIndexOf(".");
      val ext = file.getName.substring(extPos + 1);
      
      if (!ext.equals("txt")) return;
      if (EXCEPTION_RESOURCES.contains(file.getName)) return;
      
      val words = readWords(file.getPath).toList.sorted

      val out = new FileOutputStream(file.getPath)

      words.foreach {
        word: String => out.write((word + "\n").getBytes)
      }
      out.close()
    }
  }

  def run {
    val directoryName = "src/main/resources/org/openkoreantext/processor/util/";
    val directory = new File(directoryName);
    work(directory);
  }
}
