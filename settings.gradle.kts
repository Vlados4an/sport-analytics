rootProject.name = "sport-analytics"

// Tools
include("tools:jooq-generator")

// Libraries
include("libraries:di")
include("libraries:noarg")
include("libraries:environment")
include("libraries:runtime")
include("libraries:jackson-config")
include("libraries:database-utils")

// Service modules
include("services:sport-analytics:sport-analytics-api")
include("services:sport-analytics:sport-analytics-jooq")
include("services:sport-analytics:sport-analytics-core")
include("services:sport-analytics:sport-analytics-migration")
include("services:sport-analytics:sport-analytics-service")
