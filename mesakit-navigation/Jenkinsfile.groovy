////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


node('master') {
    def mvnHome = tool 'Maven 3.3.9'
    def mvn = "${mvnHome}/bin/mvn"

    stage('Checkout') {
        checkout scm
    }

    stage('Build') {
        sh "${mvn} -P shade clean package"
        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        step([
                $class       : 'JacocoPublisher',
                execPattern  : '**/target/coverage-reports/jacoco-unit.exec',
                classPattern : '**/target/classes',
                sourcePattern: '**/src/main/java'
        ])
    }

    stage('Publish') {
        def repository = "telenav.central::default::http://nexus.telenav.com:8081/nexus/content/repositories/snapshots"
        sh "${mvn} -P shade deploy -DaltDeploymentRepository=${repository} -DskipTests"
        archiveArtifacts '**/target/*.jar'
    }
}
