# A Mock Library in Scala

This is mock library that I wrote in Scala to 

See `src/main/scala/Mock.scala` for the bulk of the code,
and `src/main/scala/Main.scala` for example usage

## Dependencies

Running this requires having `sbt` [installed](https://www.scala-sbt.org/1.x/docs/Setup.html).

## Running the code

Run `sbt` first. Once in the `sbt` console, use `run` to actually
run the code we have here.

A quirk of how macros work is that if you modify the macro code, you
need to comment out usages of it first, run the code, and then uncomment
the usages to run the code properly. Otherwise, you will see an error like:

```
[error] /home/sid/src/scala_testing_stuff/testmocks/src/main/scala/Main.scala:14:20: macro implementation not found: mock
[error] (the most common reason for that is that you cannot use macro implementations in the same compilation run that defines them)
```
