# Changelog

`com.github.wikilayer:wlmarkdown-kotlin`, a Kotlin library at
[github.com/wikilayer/wlmarkdown-kotlin](https://github.com/wikilayer/wlmarkdown-kotlin),
built on [commonmark-java](https://github.com/commonmark/commonmark-java) for JDK 17
and served by JitPack at the name of its tag. The README carries the lines to put in
a build file, so that they are in one place rather than two.

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
numbers move together to say so: 0.6 here is 0.6 there and in
[the Swift port](https://github.com/wikilayer/wlmarkdown-swift). What they promise
is agreement on the corpus; where the parsers underneath differ without changing an
answer, the README says so. Which version of commonmark-java this is built against
is in `build.gradle.kts`, where it cannot go stale.

The version is 0.x because the shape is still settling: every project that has taken
these libraries so far has moved something in their API rather than working around
it, so a minor may still change an answer you relied on. Read the entry before
taking a new one.

Signatures are not repeated here; the README shows the calls in use. This file says
only what changed between versions and what that asks of you.

Changes are documented here in the format of
[Keep a Changelog](https://keepachangelog.com/).

## [v0.7.0] - 2026-09-16

### Added

- `Dialect.plainText(source)` turns markdown into reader-visible text for search,
  previews and indexing, following the shared `plain_text.yaml` corpus.
- A generated Dokka API reference, published through GitHub Pages.

## [v0.6.0] - 2026-09-14

### Added

- The port. It answers the whole corpus the leading port answers: callouts, map
  embeds, points nowhere on Earth, links naming a node, and the quotes the dialect
  turns down.
- It opens at the minor the other ports are on rather than at zero, because a
  matching major and minor is what tells a reader the behaviour is the same.
- `Dialect().parser()`, for a host that walks its own tree. It carries the GFM
  extensions the dialect expects, bare-URL linking among them, and the source spans
  a `Reading` reads a marker line from. A tree parsed without them answers
  differently, so take this parser rather than building one beside it.
- `children()`, an extension on commonmark's `Node`, because a host asking a
  `Reading` about a quote has to find the quote first.
- A test that asks the leading port for each file copied from it and compares them
  on disk, by path, so a copy nobody refreshed goes red here rather than answering an
  older dialect in silence. It reaches the network, and `make test` therefore does
  too.
- commonmark and kotlinx-serialization-core are exported rather than hidden: a host
  holds commonmark nodes to ask about, and `Found` is `@Serializable`.

[v0.7.0]: https://github.com/wikilayer/wlmarkdown-kotlin/releases/tag/v0.7.0
[v0.6.0]: https://github.com/wikilayer/wlmarkdown-kotlin/releases/tag/v0.6.0
