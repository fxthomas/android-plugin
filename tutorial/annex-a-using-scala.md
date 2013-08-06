---
layout: post
title: Annex A. Using Scala
subtitle: Useful features for Scala on Android
---

This section will explore how Scala helps Android development. For a better
understanding, go to the [Scala website](http://www.scala-lang.org).

# General language features

Here are a few things you can do in Scala. This list is of
course not exhaustive, but will give you pointers in the right direction.

## Options, Lists and `for` loops

`Option` types are the recommended way, in Scala, of handling `null` pointers denoting the absence
of an object.

For example, take this Java code :

```java
String a = "Hello, world";
String b = null;

if (a != null) System.out.println(a.toLowerCase());
if (b != null) System.out.println(b.toLowerCase());
```

In Java, you usually add `if` statements to check whether an object is `null`,
but the compiler just lets you do anything and won't complain if you don't,
often resulting in the dreaded `NullPointerException`.

In Scala, you can convert all these pesky `null` objects into `Option` objects,
which are much easier to use and chain together. Also, the compilation just
fails if you don't use them correctly, instead of crashing at runtime.

Example :

```scala
val a = Option("Hello world")    // There is something in `a`
val b = Option[String](null)     // There is nothing in `b`

for (va <- a) println(va.toLowerCase)  // Prints "Hello world"
for (vb <- b) println(vb.toLowerCase)  // Does nothing
```

`Option` objects are of two types : `Some(yourObject)` when `yourObject`
exists, and `None` when the object doesn't.

`Option` objects are very similar to lists that can have either one or zero
element, and many things that work with lists also work with `Option`,
including the `for` keyword, as we just saw :

```scala
val a = Some("Hello, world")    // Creates an Option
val b = List("Hello", "world")  // Creates a list with 2 items

for (va <- a) println(va)  // Prints "Hello, world"
for (vb <- b) println(vb)  // Prints "Hello", then "world"
```

## Closures

If you ever did a little bit of JavaScript, you're already familiar with
closures, often called "anonymous" functions. Instead of this Java snippet,
frequently found in Android :

```java
runOnUiThread(new Runnable() {
  public void run() {
    ((TextView)findViewById(R.textView)).setText("Hello, world!");
  };
});
```

...you would do this (using the [Scaloid](http://www.scaloid.org) library) :

```scala
runOnUiThread( findView(TR.textView).setText("Hello, world!") )
```

How does it work? the `findView(...)` code is treated by Scala as a `function`.
Under the hood, `runOnUiThread` just takes this function and executes it, just
as if it were a regular function.

Here is a simpler example :

```scala
// This is a function, that takes
// another function (`myFunction`) as an argument
def doSomething(myFunction: => Unit) = {
  println("Doing something")
  myFunction
}

// Will output "Doing something", then "Hello"
doSomething( println("Hello") }
```

Note the `=> Unit` type, which is Scala's way of saying : "This argument is a
function that takes no argument and returns nothing" (`Unit` is equivalent to
`void`).

## Implicits

In Java, you pass the context in every method as an object. In Android, you
often find it as a `Context` object.

In Scala, this context can be passed implicitly, and just be captured whenever
needed. Example :

```scala
// In a library, somewhere, there is a function that needs a Context to do its work
object MyHelperFunctions {
  def intent[T](implicit ctx: Context) =
    new Intent(ctx, classOf[T])
}

// MyActivity needs to use it
class MyActivity extends Activity {
  // Declares that the context is implicit
  implicit val ctx = getContext

  // Import the helper functions
  import MyHelperFunctions._
  ...

  // No need to specify the Context
  intent[MyActivity]

  // Equivalent to this
  new Intent(getContext, classOf[MyActivity])
}
```

Some more examples can be found [here](https://github.com/pocorall/scaloid#context-as-an-implicit-parameter).

# IO and asynchronous operations

I do a lot of my work in futures.

What is a future, you may want to ask? Well, a `Future` object is an object
whose value is potentially "not there yet". Remember when we talked about
`Option` a few sections before this? A future is quite similar.

Futures are great because they **don't block the main UI thread**. As any
Android developer will tell you, this is of paramount importance.

This simple example will download the front page of Google into a String, in a
background thread :

```scala
// We need this to be able to download things
import scala.io.Source

// We need this to be able to create a future
import scala.concurrent.future._

// This describes how the future is executed
// (By default, in a thread pool)
import ExecutionContext.Implicits.global

// Start the download in the background
val f = future { Source.fromURL("http://google.com") }

// Do something when it's done
for (content <- f) println(content)
```

Noticed the `for` keyword here? It's used _exactly the same way_ as with
`Option` and `List` objects.

# Typed resources

Typed resources are specific to SBT-Android : the plugin generates, along with
the traditional `R.java` file, a file called `TR.scala`, containing
automatically generated code to handle resources in a type-safe way. You don't
have to do anything to enable this feature.

_**Note:** At the time of writing this tutorial, typed resources only work
inside Activities. Pull requests are welcome to extend that feature._

To use it, make your Activity subclass extend the `TypedActivity` trait :

```scala
class MyActivity extends Activity with TypedActivity {
  ...
}
```

Anywhere inside the `MyActivity` class, instead of using `findViewById` and
_then_ casting it to the right type, you can use the `findView` method to get a
view of the right type :

```scala
findView(TR.myTextView).setText("Hello, world!")
```

Compare with the Java version, that requires an unsafe cast :

```java
((TextView)findViewById(R.myTextView)).setText("Hello, world")
```

# Noteworthy libraries

Here are a few Scala libraries you might want to take a look at :

  * [Scaloid](http://www.scaloid.org): A great general-purpose Scala-ification
    of the Android APIs

  * [Macroid](https://github.com/stanch/macroid): An Android layout DSL and advanced concurrency utilities

  * [Scala-IO](http://www.scala-io.org): For doing all kinds of IO operations
