#!/bin/sh

# Fail on a single failed command
set -eo pipefail

# ==========================================================
# Generic run script for running arbitrary Java applications
#
# Source and Documentation can be found
# at https://github.com/fabric8io/run-java-sh
#
# ==========================================================

# Error is indicated with a prefix in the return value
check_error() {
  local msg=$1
  if echo ${msg} | grep -q "^ERROR:"; then
    echo ${msg}
    exit 1
  fi
}

# The full qualified directory where this script is located
get_script_dir() {
  # Default is current directory
  local dir=`dirname "$0"`
  local full_dir=`cd "${dir}" ; pwd`
  echo ${full_dir}
}

load_env() {
  local script_dir=$1

  # Configuration stuff is read from this file
  local run_env_sh="run-env.sh"

  # Load default default config
  if [ -f "${script_dir}/${run_env_sh}" ]; then
    source "${script_dir}/${run_env_sh}"
  fi
}

# Check for standard /opt/run-java-options first, fallback to run-java-options in the path if not existing
run_java_options() {
  if [ -f "/opt/run-java-options" ]; then
    echo `sh /opt/run-java-options`
  else
    which run-java-options >/dev/null 2>&1
    if [ $? = 0 ]; then
      echo `run-java-options`
    fi
  fi
}

# Combine all java options
get_java_options() {
  local dir=$(get_script_dir)
  local java_opts
  local debug_opts
  if [ -f "$dir/java-default-options" ]; then
    java_opts=$($dir/java-default-options)
  fi
  if [ -f "$dir/debug-options" ]; then
    debug_opts=$($dir/debug-options)
  fi
  # Normalize spaces with awk (i.e. trim and elimate double spaces)
  echo "${JAVA_OPTIONS} $(run_java_options) ${debug_opts} ${java_opts}" | awk '$1=$1'
}

# Start JVM
startup() {
  # Initialize environment
  load_env $(get_script_dir)

  local args

  cd ${BROKERS_HOME}

  JAVA_ARGS=$(get_java_options)

  # Update JAVA_ARGS in artemis profile
  if [ "${JAVA_ARGS}x" != "x" ]; then
    sed -i "s/^JAVA_ARGS=.*$/JAVA_ARGS=${JAVA_ARGS}/" ${BROKERS_HOME}/${BROKER_NAME}/etc/artemis.profile
  fi

  exec ${BROKERS_HOME}/${BROKER_NAME}/bin/artemis run $*
}

# =============================================================================
# Fire up
startup $*
