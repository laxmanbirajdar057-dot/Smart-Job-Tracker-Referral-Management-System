package com.job.tracker.util;

import java.util.Set;

/**
 * Curated dictionary of tech skills/keywords used for:
 *  - Resume <-> Job Description match scoring (MatchScoreService)
 *  - ATS keyword-density checks (AtsCheckerService)
 *
 * Keeping this as a plain whitelist (instead of raw NLP tokenizing) means every
 * match is explainable: a score of 71% means exactly 71% of these specific terms
 * were found, not a fuzzy statistical guess.
 */
public class SkillKeywords {

    public static final Set<String> DICTIONARY = Set.of(
            "java", "spring boot", "spring security", "spring data jpa", "hibernate",
            "mysql", "postgresql", "rest api", "restful", "microservices", "jwt", "oauth",
            "docker", "kubernetes", "kafka", "rabbitmq", "redis",
            "junit", "mockito", "maven", "gradle", "git", "github",
            "aws", "azure", "gcp", "ci/cd", "jenkins", "javascript", "typescript", "react", "angular",
            "sql", "nosql", "mongodb", "design patterns", "multithreading", "concurrency",
            "agile", "scrum", "linux", "postman", "swagger", "openapi",
            "html", "css", "node.js", "graphql", "unit testing", "integration testing",
            "system design", "data structures", "algorithms", "oop", "solid principles"
    );
}
