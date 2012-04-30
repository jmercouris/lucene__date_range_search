package org.apache.lucene.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.sql.Timestamp;
import java.io.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexOutput;


public class
DateIndexPartition
{
    
    
	private FSDirectory dirp = null;
	public final static int NUM_DATES = 375950;
    
    
	public DateIndexPartition(FSDirectory d) 
	{
		dirp = d;
	}
    
	public DocNode[] arr = new DocNode[NUM_DATES];
    
	public void
	close()
	{
		try {
			int i = 0;
			FileWriter fw = new FileWriter(dirp.getDirectory().getAbsolutePath() + "/_0_0.dtx");
			BufferedWriter bw = new BufferedWriter(fw);
            		//System.out.println("ARI");
			while (i < 375950) {
				if (arr[i] != null) {
					bw.write(i + ";");
					DocNode dncur = arr[i];
					//System.out.println(dncur.next);
					while (dncur.next != null) {
						dncur = dncur.next;
						bw.write(dncur.docno + ";");
						//System.out.println("Wrote: " + dncur.docno);
					}
					bw.write("\n");
				}
				i++;
			}
			bw.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		};
	}
    
    
	public void
	addDocument(Iterable<? extends IndexableField> doc)
	{
		//System.out.println("Add Document Called");
		IndexableField fl = ((Document)doc).getField("date");
		IndexableField dfl = ((Document)doc).getField("DOCNO");
		if (fl != null && dfl != null) {
            
			String dt = fl.stringValue();
			String dn = dfl.stringValue();
            
			//System.out.println("DN: " + dn);
			//System.out.println("DT: " + dt);
            
			DateFormat formatter;
			Date date = null;
			formatter = new SimpleDateFormat("yyyyMMdd");
            
			try {			
				date = formatter.parse(dt);
				long unix_day_l = (date.getTime() - 18000000)/1000/60/60/24;
				int unix_day = (int)unix_day_l;
				DocNode dnode = new DocNode();
				dnode.docno = dn;
				if (arr[unix_day] == null) {
					arr[unix_day] = dnode;
				} else {
					DocNode dncur = arr[unix_day];
					while (dncur.next != null) {
						dncur = dncur.next;
					}
					dncur.next = dnode;
				}
				
			} catch (Exception e) 
            {
                System.out.println(e.getMessage());
            };
		}
	}
}
