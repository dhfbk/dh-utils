  174  java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.FindMultiWords -i all-ok-tokenized.txt --word-freq wf.txt --bigram-freq bf.txt --trigram-freq tf.txt
  603  java -Xmx6G -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.FindMultiWords -i finalT.txt -o db --threads 6 -f -h
  604  java -Xmx6G -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.FindMultiWords -i finalT.txt -o db --threads 6 -f -m
  883  java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar 
  884  java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GUparser
  885  java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GUparser -i gazzettaufficiale -o gazzettaufficiale.txt -t 8
  890  java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GUparser -i gazzettaufficiale -o gazzettaufficiale.txt -t 8
  892  java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord
  893  # java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i gazzettaufficiale.txt
  895   java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i gazzettaufficiale.txt -c stanford.properties
  896   java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i gazzettaufficiale.txt -p stanford.properties
  897   java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i gazzettaufficiale.txt -p stanford.properties -o gazzettaufficiale.tokens.txt
  901   java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i gazzettaufficiale.txt -p stanford.properties -o gazzettaufficiale.tokens.txt
  902   java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar:tint-runner-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i gazzettaufficiale.txt -p stanford.properties -o gazzettaufficiale.tokens.txt
  907   java -cp gigaword-1.0-SNAPSHOT-jar-with-dependencies.jar:tint-runner-1.0-SNAPSHOT-jar-with-dependencies.jar eu.fbk.dh.utils.resources.GigaWord -i ../subtitles/OpenSubtitles2016/it.txt -p stanford.properties -o ../subtitles/OpenSubtitles2016/it-tokens.txt
 1015  history|grep java|grep gigawo
(reverse-i-search)`cat ': ^Ct leggi/gazzettaufficiale.tokens.txt paisa/paisa-tokenized.txt subtitles/OpenSubtitles2016/it-tokens.txt > tantaroba.txt

