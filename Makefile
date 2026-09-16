CORPUS = ../wlmarkdown/corpus
RULES = src/main/resources
CASES = src/test/resources

.DEFAULT_GOAL := build

.PHONY: format lint comments test-build test docs build sync-corpus

format:
	./gradlew ktlintFormat

comments:
	commentcensor src

lint: comments
	./gradlew ktlintCheck detekt

test-build:
	./gradlew compileKotlin compileTestKotlin

test:
	./gradlew test corpusIsCurrent

docs:
	./gradlew dokkaGeneratePublicationHtml

build: lint test-build test docs
	./gradlew build

sync-corpus:
	cp $(CORPUS)/rules.yaml $(RULES)/
	cp $(CORPUS)/dialect.yaml $(CASES)/
	cp $(CORPUS)/plain_text.yaml $(CASES)/
