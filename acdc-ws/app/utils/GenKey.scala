object GenKey {

  def main(args: Array[String]): Unit = {

    Seq(
      "0OuYBe5KX1QmyjXTMmCYBm1byEJ7FGfR",
      "qdCmPXnIJOs8eP82PKJLaHuugb0fouav",
      "bT15TkL2w5y7HNRYSIjbnRoJ5HrVfqgw",
      "CJV7R0kqKgM6XQ5KTiyApR2lfFXQKBWJ"
    ).foreach(i => println(s"$i => ${Authorization.convertToSha256(i)}"))
  }
}