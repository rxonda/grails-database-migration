/*
 * Copyright 2015 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugins.databasemigration.command

import groovy.transform.CompileStatic
import liquibase.database.Database
import org.grails.plugins.databasemigration.DatabaseMigrationException

@CompileStatic
class DbmGenerateChangelogCommand implements ScriptDatabaseMigrationCommand {

    String filename
    String defaultSchema
    String dataSource
    boolean add

    void handle() {
        def filename = args[0]
        def defaultSchema = commandLine.optionValue('defaultSchema') as String
        def dataSource = commandLine.optionValue('dataSource') as String

        def changeLogFile = resolveChangeLogFile(filename, dataSource)
        if (changeLogFile) {
            if (changeLogFile.exists()) {
                if (commandLine.hasOption('force')) {
                    changeLogFile.delete()
                } else {
                    throw new DatabaseMigrationException("ChangeLogFile ${changeLogFile} already exists!")
                }
            }
            if (!changeLogFile.parentFile.exists()) {
                changeLogFile.parentFile.mkdirs()
            }
        }

        withDatabase(defaultSchema, dataSource, getDataSourceConfig(dataSource)) { Database database ->
            doGenerateChangeLog(changeLogFile, database)
        }

        if (changeLogFile && hasOption('add')) {
            appendToChangeLog(getChangeLogFile(dataSource), changeLogFile)
        }
    }
}
