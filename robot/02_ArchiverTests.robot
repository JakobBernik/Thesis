*** Comments ****
archiver tests

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

Run archiver test
  [Arguments]   ${testName}
  Run Java test  thesis.testing.system.archivingTests.ArchiverTests   ${testName}

*** Test Cases ***

Invalid command test
  [Template]   Run archiver test
  [Documentation]   tests if exception is thrown for unknown command
  invalidCommandTest

Invalid params for store test
  [Template]   Run archiver test
  [Documentation]   tests if exception is thrown for valid store command, wrong params
  validStoreCommandInvalidParamsTest

Invalid params for get test
  [Template]   Run archiver test
  [Documentation]   tests if exception is thrown for valid get command, wrong params
  validGetCommandInvalidParamsTest

Invalid params for search test
  [Template]   Run archiver test
  [Documentation]   tests if exception is thrown for valid search command, wrong params
  validSearchCommandInvalidParamsTest

Get existing data packets test
  [Template]   Run archiver test
  [Documentation]   tests if requesting existing data returns it
  getValidDataPacketsTest

Get non-existing data packets test
  [Template]   Run archiver test
  [Documentation]   tests if exception is thrown when requesting non-existing data
  getInvalidDataPacketsTest

Store valid data test
  [Template]   Run archiver test
  [Documentation]   tests if storing valid data works
  storeValidDataPointTest

Store invalid data test
  [Template]   Run archiver test
  [Documentation]   tests if storing invalid data throws an exception
  storeInvalidDataPointTest

Search by id test
  [Template]   Run archiver test
  [Documentation]   tests if searching by point id works
  searchValidDataPacketsDpTest

Search by date test
  [Template]   Run archiver test
  [Documentation]   tests if searching by capture date works
  searchValidDataPacketsDateTest

Invalid search test
  [Template]   Run archiver test
  [Documentation]   tests if searching with invalid query throws an exception
  searchInvalidDataPackets


