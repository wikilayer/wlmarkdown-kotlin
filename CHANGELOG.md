# Changelog

A Kotlin library, `org.wikilayer:wlmarkdown-kotlin`, at
[github.com/wikilayer/wlmarkdown-kotlin](https://github.com/wikilayer/wlmarkdown-kotlin),
built on [commonmark-java](https://github.com/commonmark/commonmark-java):

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.wikilayer:wlmarkdown-kotlin:0.6.0")
}
```

It recognises the markdown dialect of WikiLayer, a wiki whose pages are a tree of
nodes: GitHub-flavoured markdown plus callouts, map embeds, and links naming a node
instead of a URL.

```markdown
> [!WARNING]
> This cannot be undone.

Start at [the front page](page:home), or at [one paragraph](block:50386) of it.
```

It recognises and does nothing else. What title that callout wears, which icon and
colour it gets, which address `page:home` resolves to: a web page answers each of
those one way and a phone app another, so each belongs to the application holding
the pages rather than to a parser.

This is a port. [wlmarkdown](https://github.com/wikilayer/wlmarkdown) leads, every
port reads the same rules and answers the same corpus, and the major and minor
numbers move together to say so. What they promise is agreement on the corpus, and
the corpus does not reach everything: the difference it cannot reach is named below,
and the README says where the parsers underneath differ. Which version of
commonmark-java this is built against is in `build.gradle.kts`, where it cannot go
stale.

The version is 0.x because the shape is still settling: every reader of these
libraries so far has moved something in their API rather than working around it, so
a minor may still change an answer you relied on. Read the entry before taking one.

## Where the ports do not agree

One difference is left, and no corpus case can reach it, so a green corpus does not
prove the ports answer alike. It is open in 0.6.0:

- A bare URL is a link in Go and plain words here. The dialect asks every port to
  switch linkifying on; this port does not, because the Go port leaves such a link
  out of `Found` and switching it on here would put it in.

Signatures are not repeated here; the README carries an example of each call. This
file says only what changed between versions and what that asks of you.

Changes are documented here in the format of
[Keep a Changelog](https://keepachangelog.com/).

## 0.6.0 - 2026-09-14

### Added

- The port. It answers the whole corpus the leading port answers, callouts, map
  embeds, points nowhere on Earth, links naming a node, and the quotes the dialect
  turns down, and it starts at the minor the other ports are on rather than at zero,
  because a matching major and minor is what says the behaviour is the same.
- `Dialect.parser()`, for a host that walks its own tree. It carries the GFM
  extensions the dialect expects and the source spans a `Reading` reads a marker
  line from. A tree parsed without them answers differently, so take this parser
  rather than building one beside it.
