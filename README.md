# wlmarkdown-kotlin

The WikiLayer markdown dialect in Kotlin: GitHub-flavoured markdown, and then the
constructs the dialect adds of its own. It is the Kotlin port of
[wlmarkdown](https://github.com/wikilayer/wlmarkdown), which leads, and it answers
the same corpus of cases the Go port answers.

```kotlin
val found = Dialect().recognise("> [!TIP]\n> Try the shorter form.\n")
```

`Found` comes back flat and in document order, one entry per construct: a callout
with its class, a map with its point and caption, a link with the scheme it names
and the destination exactly as written, and a point nowhere on Earth as the words
it was written with.

## What it recognises

A blockquote whose first line is exactly `[!NOTE]`, `[!TIP]`, `[!IMPORTANT]`,
`[!WARNING]` or `[!CAUTION]` is a callout of that class. A marker sharing its line
with words, or written in lower case, leaves an ordinary quote.

A `[!MAP]` marker followed by a line of two numbers is a map embed, and whatever
follows is its caption. Both numbers are digits carrying an optional sign and an
optional fraction, and nothing else: no exponent, no hexadecimal, no infinity. They
come back as the source wrote them, digit for digit, because rounding a coordinate
moves the point.

A latitude may go as far as 90 and a longitude as far as 180, the poles and the
meridian included. Past that the pair is still read and there is nowhere to put it,
so the quote comes back as `kind = "unreadable"` carrying the words as they were
written rather than as a map of a place the page does not name.

A link may name a node instead of a URL, under the scheme `page:` or `block:`. The
destination comes back character for character; which names exist is a question the
store answers.

`markers`, `classes` and `schemes` name what the dialect opens constructs with and
what can come back in `Found`. Read them rather than writing down what is in them
today.

## Asking about a document you parsed yourself

A host that builds its own tree holds a `BlockQuote` and needs to know what it is.
`Reading` answers that, over the source the document was parsed from:

```kotlin
val reading = Reading(page.body)
for (quote in reading.document.children().filterIsInstance<BlockQuote>()) {
    reading.place(quote)?.let { }         // lat, lng, caption
    reading.unreadable(quote)?.let { }    // a point nowhere on Earth
    reading.calloutClass(quote)?.let { }  // note, tip, warning …
}
```

Parse with `Dialect().parser()` if you build the tree yourself. It carries the GFM
extensions the dialect expects and the source spans a `Reading` reads a marker line
from, and a tree parsed without them answers differently.

A quote written as a map whose point is outside `90` and `180` is not a place, so
`place` stays silent about it and `unreadable` hands back the words as they stand in
the source. Show them: the only person who can fix such coordinates is the one who
typed them.

`opensAConstruct` says whether a quote carries any of the dialect's markers.
`Dialect.scheme` names the scheme of a destination, or none.

`reading.declined()` hands back the quotes the dialect turned down, one entry per
quote with the marker it carried: a map whose coordinates did not read, or a callout
written inside another callout's quote. Deciding that from outside would mean
writing the dialect's rule for what opens a construct a second time.

A `Reading` is built on one source string and answers by position in it, so it must
be the string its document was parsed from.

## What it does not do

It recognises. A title for a callout, an icon, a colour, a link resolved against a
store: each of those belongs to whoever holds the pages, because a web page answers
them one way and a phone app another.

## The corpus

`src/main/resources/rules.yaml` holds what the dialect knows and
`src/test/resources/dialect.yaml` the cases that define it. Both are copies of the
files in the leading port, refreshed with `make sync-corpus`, and the whole corpus
runs here on every build. A case answered differently by two ports goes red rather
than reaching a reader.

## Where the ports differ

All three read the same rules and answer the same cases, but they stand on different
parsers: goldmark, [swift-markdown](https://github.com/swiftlang/swift-markdown) and
[commonmark-java](https://github.com/commonmark/commonmark-java). The differences
below are theirs rather than the dialect's.

A marker line is read from the block's source spans rather than from the parsed
text, because a text node has its trailing space trimmed off and `[!NOTE] ` with a
space after it is not a marker. The spans carry the line without its quote marker
and with the blanks it ended on, which is exactly what the leading port reads.

A table's parts arrive from the GFM extension as inline nodes although they stand
beside one another, so their words are spaced here as a block's are. Left as the
extension declares them, a row of cells would come back as one run of letters.

One difference is the dialect's and not a parser's, and it is open: a bare URL is a
link on the site and plain words here, because the dialect asks a port to switch
linkifying on and this port does not, so that a link the Go port leaves out of
`Found` is left out here too. It has no corpus case, which is why the corpus alone
does not prove the ports agree.

## Running it

```sh
make test          # the corpus, plus the rules tests
make lint          # commentcensor, ktlint and detekt
make sync-corpus   # refresh rules.yaml and dialect.yaml from the leading port
```
