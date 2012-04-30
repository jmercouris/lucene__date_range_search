package org.apache.lucene.search;

import java.io.*;
import java.lang.Integer;
import java.util.ArrayList;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.index.DocNode;
import org.apache.lucene.index.DateIndexPartition;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.document.Document;


public class DateIndexSearcher
{
	int num_dates = DateIndexPartition.NUM_DATES;
	String[] currentIntersection;
	public DocNode[] arr;
	public int start_range = 0;
	public int end_range = 0;
	FSDirectory dirp;
    
	public DateIndexSearcher(FSDirectory fd)
	{ 
		dirp = fd;
		try {
            
			//System.out.println("Constructed");
			arr = new DocNode[num_dates];
			FileInputStream fs = new FileInputStream(
                                                     fd.getDirectory().getAbsolutePath() 
                                                     + "/_0_0.dtx");
			DataInputStream ds = new DataInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(ds));
			String strLine;
			String[] strtmp;
			while((strLine = br.readLine()) != null) {
				strtmp = strLine.split(";");
				int cur_ix = Integer.parseInt(strtmp[0]);
				DocNode node = new DocNode();
				node.docno = strtmp[1];
				//System.out.println("Adding init doc " + node.docno);
				arr[cur_ix] = node;
				for (int i = 2; i < strtmp.length; i++) {
					node.next = new DocNode();
					node.next.docno = strtmp[i];
					//System.out.println("Linking doc " + node.next.docno + " to " + node.docno);
					node = node.next;
				}
			}
			ds.close();
			verify();
		} catch (Exception e) {
		};
	}
    
	public void verify()
	{
		try {
		    int i = 0;
		    FileWriter fw = new FileWriter(dirp.getDirectory().getAbsolutePath() + "/verification");
		    BufferedWriter bw = new BufferedWriter(fw);
		    while (i < 375950) {
			if (arr[i] != null) {
			    bw.write(i + ";");
			    DocNode dncur = arr[i];
			    while (dncur.next != null) {
				bw.write(dncur.docno + ";");
				dncur = dncur.next;
			    }
			    bw.write("\n");
			}
			i++;
		    }
		    bw.close();
		    
		} catch(Exception fu) {};
	}
    
    public void setRange(int inputStartRange, int inputEndRange)
    {
        start_range = inputStartRange;
        end_range = inputEndRange;
        
        ArrayList<String> myArr = new ArrayList<String>();
        DocNode tmp;
        for (int i = start_range; i < end_range; i++)
        {
            if (arr[i] != null)
            {
                //System.out.println("Date is: " + i);
                tmp = arr[i];
                myArr.add(tmp.docno);
                while(tmp.next != null)
                {
                    tmp = tmp.next;
                    myArr.add(tmp.docno);
                }
            }
        }
        
        currentIntersection = myArr.toArray(new String[myArr.size()]);
        
        //System.out.println("ArrayList Size: " + myArr.size());
        //System.out.println("Current Intersection Size: " + currentIntersection.length);
	/*
        for (int j = 0; j < currentIntersection.length; j++)
        {
            System.out.println("Intersection " + j + ": " + currentIntersection[j]);
        }
	*/
        
    }
    
    public TopDocs applyDateRangeFilter(TopDocs inputTopDocs, IndexSearcher inputIndexSearcher)
    {
        try
        {
            //System.out.println("Apply Date Range Filter");
            ArrayList<ScoreDoc> outPutArray = new ArrayList<ScoreDoc>();

            ScoreDoc[] hits = inputTopDocs.scoreDocs;
            ScoreDoc[] outPutHits;
            
            for (int i = 0; i < hits.length; i++)
            {
                // Take Document
                Document doc = inputIndexSearcher.doc(hits[i].doc);
                String path = doc.get("DOCNO");
                //System.out.println("Path:" + path);
                
                // Conduct Intersection
                for (int j = 0; j < currentIntersection.length; j++)
                {
                    if (path.equalsIgnoreCase(currentIntersection[j]))
                    {
                        //System.out.println("Match Hit");
                        outPutArray.add(hits[i]);
                    }
                }
            }

            // Aggregate Results into an Array
            outPutHits = outPutArray.toArray(new ScoreDoc[outPutArray.size()]);
            inputTopDocs.scoreDocs = outPutHits;
        }
	catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return inputTopDocs;
    }    
}
