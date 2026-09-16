CORPUS = ../wlmarkdown/corpus
RULES = src/main/resources
CASES = src/test/resources
COMMENTCENSOR_VERSION ?= v0.3.0

.DEFAULT_GOAL := build

.PHONY: install-tools format lint comments test-build test docs build sync-corpus

install-tools:
	python3 -m pip install --quiet git+https://github.com/botforge-pro/commentcensor.git@$(COMMENTCENSOR_VERSION)

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
