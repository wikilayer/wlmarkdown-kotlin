CORPUS = ../wlmarkdown/corpus
RULES = src/main/resources
CASES = src/test/resources

.PHONY: format lint comments test-build test build sync-corpus

format:
	./gradlew ktlintFormat

comments:
	commentcensor src

lint: comments
	./gradlew ktlintCheck detekt

test-build:
	./gradlew compileKotlin compileTestKotlin

test:
	./gradlew test

build:
	./gradlew build

sync-corpus:
	cp $(CORPUS)/rules.yaml $(RULES)/
	cp $(CORPUS)/rules.yaml $(CASES)/
	cp $(CORPUS)/dialect.yaml $(CASES)/
