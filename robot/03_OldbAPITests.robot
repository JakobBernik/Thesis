*** Comments ****
oldb API tests

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

Run oldbAPI test
  [Arguments]   ${testName}
  Run Java test  thesis.testing.system.oldbTests.OldbApiTests   ${testName}

*** Test Cases ***

Add valid data point test
  [Template]   Run oldbAPI test
  [Documentation]   Checks if adding valid data points works as it should
  addValidDataPointTest

Add invalid data point test
    [Template]   Run oldbAPI test
    [Documentation]   Checks if adding invalid data point triggers exception
    addInvalidDataPointTest

Add invalid data point test
    [Template]   Run oldbAPI test
    [Documentation]   Checks if requesting existing data point returns correct one
    getExistingDataPointTest

Add invalid data point test
    [Template]   Run oldbAPI test
    [Documentation]   Checks if requesting non-existing data point triggers exception
    getMissingDataPointTest

Add invalid data point test
    [Template]   Run oldbAPI test
    [Documentation]   Checks if deleting existing data point works
    deleteExistingDataPointTest

Add invalid data point test
    [Template]   Run oldbAPI test
    [Documentation]   Checks if deleting non-existing data point triggers exception
    deleteMissingDataPointTest