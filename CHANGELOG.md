# Changelog

Notable changes to `WLMarkdown` are documented here in the format of
[Keep a Changelog](https://keepachangelog.com/). The Go package
[wlmarkdown](https://github.com/wikilayer/wlmarkdown) leads the shared rules and
corpora; matching major and minor versions promise agreement on those cases.

The package remains below 1.0 while its public API is settling. The README carries
installation and usage examples, and the generated reference carries signatures.

## [v0.7.1] - 2026-09-18

### Changed

- Documented the complete public API and clarified the package boundary, setup,
  corpus relationship, and commonmark-specific behavior. Runtime behavior is
  unchanged.
- Added the MIT license, updated commentcensor, and moved release publication from
  the Makefile into a repository workflow.

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
[v0.7.1]: https://github.com/wikilayer/wlmarkdown-kotlin/releases/tag/v0.7.1
[v0.6.0]: https://github.com/wikilayer/wlmarkdown-kotlin/releases/tag/v0.6.0
