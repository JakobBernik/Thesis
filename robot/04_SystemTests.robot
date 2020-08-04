*** Comments ****
System tests

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

Run system test
  [Arguments]   ${testName}
  Run Java test  thesis.testing.system.SystemTests  ${testName}

*** Test Cases ***

Non-subscribed point test
    [Template]   Run system test
    [Documentation]  test if new point, added to oldb that does not match capture configuration is not archived
    nonsubscribedPointAdditionTest

Subscribed point test
    [Template]   Run system test
    [Documentation]  test if new point, added to oldb that matches capture configuration is archived
    subscribedPointAdditionTest