:: The maven has a bug on Windows,
:: if the command "mvn" is used in a bat file, after it's finished, the bat will be terminated. ^
:: So use this private bat to wrap the command "mvn" and let the public bat call this bat.
mvn %*
