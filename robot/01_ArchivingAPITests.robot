*** Comments ****
archiving API tests

*** Settings ***
Library  Process
Library  String
Library  OperatingSystem
Test Timeout    2 minutes

*** Variables ***
${java_cli}    %{SCRIPTS_DIR}/java-cli.sh
${exit_code_path}  %{ROBOT_DIR}
*** Keywords ***
Run Java test
  [Arguments]  ${group}   ${testName}
  Log To Console  .
  Log To Console  Executing Java CLI test ${testName}.
  ${status}=  Run Process   ${java_cli}  thesis.testing.runner.NgRunner  ${group}   ${testName}   shell=yes
  Log To Console   ${status.stdout}
  Log To Console   ${status.stderr}
  log    ${status.stdout}
  log    ${status.stderr}
  ${exitCode}=  Get File  ${exit_code_path}/exitCode.txt
  Remove File  ${exit_code_path}/exitCode.txt
  Should Be Equal As Integers  ${exitCode}  0    Test failed, examine log in report

Run archivingAPI test
  [Arguments]   ${testName}
  Run Java test  thesis.testing.system.archivingTests.ArchivingApiTests  ${testName}

*** Test Cases ***

Get existing data packets test
  [Template]   Run archivingAPI test
  [Documentation]   Checks if requesting stored data returns correct data packet
  getExistingDataPacketsTest

Get missing data packets test
  [Template]   Run archivingAPI test
  [Documentation]   Checks if requesting non-existing data packets triggers exception
  getMissingDataPacketsTest

Store valid data packets test
  [Template]   Run archivingAPI test
  [Documentation]   Checks if storing valid data packets works as it should
  storeValidDataPacketsTest

Store invalid data packets test
  [Template]   Run archivingAPI test
  [Documentation]   Checks if storing invalid data packets triggers an exception
  storeInvalidDataPacketsTest

Search valid query test dp
  [Template]   Run archivingAPI test
  [Documentation]   Checks if searching for data by point name returns correct data packet ids
  searchValidQueryTestDp

Search valid query test date
  [Template]   Run archivingAPI test
  [Documentation]   Checks if searching for data by date of capturing returns correct data packet ids
  searchValidQueryTestDate

Search invalid query test
  [Template]   Run archivingAPI test
  [Documentation]   Checks if invalid query triggers exception
  searchInvalidQueryTest
