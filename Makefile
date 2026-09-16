CORPUS = ../wlmarkdown/corpus
RULES = src/main/resources
CASES = src/test/resources
COMMENTCENSOR_VERSION ?= v0.3.1
COMMENTCENSOR_ENV = build/commentcensor
COMMENTCENSOR = $(COMMENTCENSOR_ENV)/bin/commentcensor

.DEFAULT_GOAL := build

.PHONY: install-tools format lint comments test-build test docs build sync-corpus

install-tools:
	python3 -m venv $(COMMENTCENSOR_ENV)
	$(COMMENTCENSOR_ENV)/bin/pip install --quiet --upgrade git+https://github.com/botforge-pro/commentcensor.git@$(COMMENTCENSOR_VERSION)

format:
	./gradlew ktlintFormat

comments:
	$(COMMENTCENSOR) src

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
