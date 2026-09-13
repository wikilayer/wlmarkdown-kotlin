# wlmarkdown-kotlin

The WikiLayer markdown dialect in Kotlin: GitHub-flavoured markdown, and then the
constructs the dialect adds of its own. It is the Kotlin port of
[wlmarkdown](https://github.com/wikilayer/wlmarkdown), which leads, and it answers
the same corpus of cases the Go port and
[the Swift port](https://github.com/wikilayer/wlmarkdown-swift) answer.

```kotlin
val found = Dialect().recognise("> [!TIP]\n> Try the shorter form.\n")
```

`Found` comes back flat and in document order, one entry per construct, and `kind`
says which fields carry anything:

| `kind` | fields |
|---|---|
| `callout` | `calloutClass`, `text` |
| `map` | `lat`, `lng`, `caption` |
| `unreadable` | `text` |
| `link` | `scheme`, `destination`, `text` |

The field is `calloutClass` because `class` is a word Kotlin keeps for itself; the
corpus and the other ports call it `class`, and the YAML key is still `class`.

## What it recognises

A blockquote whose first line is exactly `[!NOTE]`, `[!TIP]`, `[!IMPORTANT]`,
`[!WARNING]` or `[!CAUTION]` is a callout of that class. A marker sharing its line
with words, or written in lower case, leaves an ordinary quote.

A `[!MAP]` marker followed by a line of two numbers is a map embed, and whatever
follows is its caption. Both numbers are digits carrying an optional sign and an
optional fraction, and nothing else: no exponent, no hexadecimal, no infinity. They
come back as the source wrote them, digit for digit, because rounding a coordinate
moves the point.

A latitude may go as far as 90 and a longitude as far as 180, both bounds included.
Past that the pair is still read and there is nowhere to put it, so the quote comes
back as `kind = "unreadable"` carrying the words as they were written rather than as
a map of a place the page does not name.

A link may name a node instead of a URL, under the scheme `page:` or `block:`. The
destination comes back character for character; which names exist is a question the
store answers. Only a link somebody wrote as one is reported: a bare address becomes
a link in the tree, because the dialect asks every port to switch linkifying on, and
it is left out of `Found`, as is `<https://example.com>` written in angle brackets.

`markers` names what the dialect opens constructs with, `classes` what a callout can
carry and `schemes` what a link of ours can name. Read them rather than writing down
what is in them today.

## Asking about a document you parsed yourself

A host that builds its own tree holds a `BlockQuote` and needs to know what it is.
`Reading` answers that, over the source string the document was parsed from:

```kotlin
val markdown = "…the text your own store holds…"
val reading = Reading(markdown)
for (quote in reading.document.children().filterIsInstance<BlockQuote>()) {
    reading.place(quote)?.let { }         // lat, lng, caption
    reading.unreadable(quote)?.let { }    // a point nowhere on Earth
    reading.calloutClass(quote)?.let { }  // note, tip, warning …
}
```

`children()` is an extension on commonmark's `Node` and this library exposes it,
because walking a tree by `firstChild` and `next` is not what the caller came here
to write.

Parse with `Dialect().parser()` if you build the tree yourself. It carries the GFM
extensions the dialect expects and the source spans a `Reading` reads a marker line
from, and a tree parsed without them answers differently.

A quote written as a map whose point is outside `90` and `180` is not a place, so
`place` stays silent about it and `unreadable` hands back the words as they stand in
the source. Show them: the only person who can fix such coordinates is the one who
typed them.

`opensAConstruct` says whether a quote carries any of the dialect's markers, and
`Dialect.scheme` names the scheme of a destination, or none:

```kotlin
Dialect().scheme("page:home")   // "page"
Dialect().scheme("https://…")   // null
```

`reading.declined()` hands back the quotes the dialect turned down, one entry per
quote with the marker it carried: a map whose coordinates did not read, or a marker
standing inside a quote the dialect made nothing of, where it opens no construct at
all. Deciding that from outside would mean writing the dialect's rule for what opens
a construct a second time.

A `Reading` is built on one source string and answers by position in it, so it must
be the string its document was parsed from.

## What it does not do

It recognises. A title for a callout, an icon, a colour, a link resolved against a
store: each of those belongs to whoever holds the pages, because a web page answers
them one way and a phone app another.

## The corpus

`src/main/resources/rules.yaml` holds what the dialect knows and
`src/test/resources/dialect.yaml` the cases that define it, with a second copy of
the rules beside the cases so the tests read them without reaching into the library.
All three are copies of the files in the leading port, refreshed with
`make sync-corpus`, which reads them from a clone of
[wlmarkdown](https://github.com/wikilayer/wlmarkdown) in the directory next to this
one. The whole corpus runs here on every build, so a case answered differently by
two ports goes red rather than reaching a reader.

## Where the ports differ

All three read the same rules and answer the same cases, but they stand on different
parsers: goldmark, [swift-markdown](https://github.com/swiftlang/swift-markdown) and
[commonmark-java](https://github.com/commonmark/commonmark-java). The differences
below are theirs rather than the dialect's, and none of them changes an answer.

A marker line is read from the block's source spans rather than from the parsed
text, because a text node has its trailing space trimmed off and `[!NOTE] ` with a
space after it is not a marker. The spans carry the line without its quote marker
and with the blanks it ended on, which is exactly what the leading port reads.

A table's parts arrive from the GFM extension as inline nodes although they stand
beside one another, so their words are spaced here as a block's are. Left as the
extension declares them, a row of cells would come back as one run of letters.

A written link is told from a linkified one by the `[` the source opens it with,
because commonmark builds the same node for both, where goldmark gives an autolink
a node of its own.

## Running it

```sh
make test          # the corpus, the rules tests, and the host-side answers
make lint          # commentcensor, ktlint and detekt
make sync-corpus   # refresh rules.yaml and dialect.yaml from the leading port
```

`commentcensor` is ours and lives outside this repository; `make test` and the
checks CI runs do not need it.
