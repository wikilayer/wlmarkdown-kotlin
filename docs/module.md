# WLMarkdown for Kotlin

WLMarkdown recognises the WikiLayer markdown dialect and extracts its reader-visible
plain text. Use `Dialect.recognise` when a host needs callouts, map embeds and node
links as structured values, and `Dialect.plainText` for search, previews and indexing.

The Go package [wlmarkdown](https://github.com/wikilayer/wlmarkdown) leads the shared
rules and corpora. Matching major and minor versions across the ports mean they
answer those cases alike.
