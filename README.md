# wlmarkdown-kotlin

[![Tests](https://github.com/wikilayer/wlmarkdown-kotlin/actions/workflows/tests.yml/badge.svg)](https://github.com/wikilayer/wlmarkdown-kotlin/actions/workflows/tests.yml)
[![Documentation](https://github.com/wikilayer/wlmarkdown-kotlin/actions/workflows/documentation.yml/badge.svg)](https://wikilayer.github.io/wlmarkdown-kotlin/)

The Kotlin implementation of the WikiLayer markdown dialect. It recognises
callouts, map embeds, and `page:` and `block:` links on top of
[commonmark-java](https://github.com/commonmark/commonmark-java). The Go package
[wlmarkdown](https://github.com/wikilayer/wlmarkdown) leads the shared rules and
test corpora.

The library targets JDK 17 and is published through JitPack:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.wikilayer:wlmarkdown-kotlin:v0.7.1")
}
```

Recognise structured constructs or extract reader-visible text:

```kotlin
import org.wikilayer.wlmarkdown.Dialect

val dialect = Dialect()
val found = dialect.recognise("> [!TIP]\n> Try the shorter form.\n")
val plain = dialect.plainText("Read **this** before `make test`.")
```

`recognise` returns a flat list in document order. Each `Found` value describes a
callout, map, unreadable map, or link. `plainText` removes markdown syntax for
search, previews, and indexing.

## What it recognises

A blockquote whose first line is exactly `[!NOTE]`, `[!TIP]`, `[!IMPORTANT]`,
`[!WARNING]`, or `[!CAUTION]` is a callout. A marker sharing its line with words or
written in another case leaves an ordinary quote.

A `[!MAP]` marker followed by a line of two numbers is a map embed. The remaining
lines of that paragraph are its caption. Coordinates contain digits with an
optional sign and fraction; their spelling is preserved. A latitude may go as far
as 90 and a longitude as far as 180. A pair outside those bounds comes back as
`kind = "unreadable"` with the words the author wrote.

A written link may name a node under the `page:` or `block:` scheme. The library
reports the destination produced by commonmark but does not resolve it against a
store. Bare URLs are linkified by the parser but omitted from `Found`, like
angle-bracket autolinks.

Read `markers`, `classes`, and `schemes` instead of copying their current values
into an application.

## Asking about a parsed document

`Reading` answers questions about a commonmark tree. Use its lazy `document`, or
parse the same source with `Dialect.parser()` so the tree has the extensions and
source spans these answers require:

```kotlin
import org.commonmark.node.BlockQuote
import org.wikilayer.wlmarkdown.Reading

val source = "> [!MAP]\n> 44.7866, 20.4489\n> Belgrade\n"
val reading = Reading(source)
val quote = reading.document.firstChild as BlockQuote
val place = reading.place(quote)
```

`place` returns valid maps, `unreadable` returns the words of an out-of-bounds map,
and `declined` reports marked quotes the dialect left unchanged.
`opensAConstruct`, `scheme`, and the `children()` extension expose the remaining
rules without making an application repeat them.

## What it does not do

The library does not render constructs, decorate callouts, or resolve links. Those
choices belong to the application holding the pages.

## The corpus

`rules.yaml`, `dialect.yaml`, and `plain_text.yaml` are copies of the leading Go
port's rules and corpora. Refresh them with `make sync-corpus`; every build verifies
the copies and their answers. The freshness check reaches the leading repository
and fails rather than accepting an unverifiable copy when the network is absent.

## Documentation

The [Dokka API reference](https://wikilayer.github.io/wlmarkdown-kotlin/) is
generated and published by GitHub Actions.

## Development

```sh
make build
make sync-corpus
```

`build` runs commentcensor, ktlint, detekt, compilation, tests, corpus
verification, Dokka, and the final jar. `sync-corpus` refreshes the shared rules
and cases from the leading Go repository.

Releases are published by the repository's
[Release workflow](https://github.com/wikilayer/wlmarkdown-kotlin/actions/workflows/release.yml),
after it repeats the complete build.

## Lines of Code

<picture>
  <source media="(prefers-color-scheme: dark)" srcset=".github/loc-history-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset=".github/loc-history-light.svg">
  <img src=".github/loc-history.svg" alt="Lines of code over time">
</picture>
