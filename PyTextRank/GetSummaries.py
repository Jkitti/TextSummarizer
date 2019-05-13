import pytextrank
from os import path
import sys
from os import listdir
from os.path import isfile, join
import os.path

SRCDIR = path.dirname(path.realpath(__file__))
JSONDIR = path.join(SRCDIR,"jsonfiles")
stage1 = path.join(SRCDIR,"stage1")
stage2 = path.join(SRCDIR,"stage2")
stage3 = path.join(SRCDIR,"stage3")
stage4 = path.join(SRCDIR,"output")
onlyfiles = [f for f in listdir (JSONDIR) if isfile(join(JSONDIR, f))]
for val in onlyfiles:
        path_stage0 = path.join(JSONDIR,val)
        path_stage1 = path.join(stage1, os.path.splitext(val)[0] + ".json")
        path_stage2 = path.join(stage2, os.path.splitext(val)[0] + ".json")
        path_stage3 = path.join(stage3, os.path.splitext(val)[0] + ".json")
        path_final = path.join(stage4, os.path.splitext(val)[0] + "_syssum1.txt")
        text = open(path.join(JSONDIR,val),'r').read()
        #print  ('\n')

        with open(path_stage1, 'w') as f:
            for graf in pytextrank.parse_doc(pytextrank.json_iter(path_stage0)):
                f.write("%s\n" % pytextrank.pretty_print(graf._asdict()))
                # to view output in this notebook
        graph, ranks = pytextrank.text_rank(path_stage1)
        pytextrank.render_ranks(graph, ranks)

        with open(path_stage2, 'w') as f:
            for rl in pytextrank.normalize_key_phrases(path_stage1, ranks):
                f.write("%s\n" % pytextrank.pretty_print(rl._asdict()))
                
        kernel = pytextrank.rank_kernel(path_stage2)

        with open(path_stage3, 'w') as f:
            for s in pytextrank.top_sentences(kernel, path_stage1):
                f.write(pytextrank.pretty_print(s._asdict()))
                f.write("\n")
                        
        phrases = ", ".join(set([p for p in pytextrank.limit_keyphrases(path_stage2, phrase_limit=12)]))
        sent_iter = sorted(pytextrank.limit_sentences(path_stage3, word_limit=150), key=lambda x: x[1])
        s = []

        for sent_text, idx in sent_iter:
            s.append(pytextrank.make_sentence(sent_text))
            
        #Write Final output to file
        graf_text = " ".join(s)
        with open(path_final, 'w') as f:
            for string in graf_text:
                f.write(string)
