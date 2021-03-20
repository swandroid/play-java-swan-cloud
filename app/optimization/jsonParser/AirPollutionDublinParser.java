package optimization.jsonParser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class AirPollutionDublinParser {
	String file;
	List<Double> concentration = new ArrayList<>();

	public AirPollutionDublinParser(String file){
		this.file = file;
	}

	public void parse() throws IOException {
		FileInputStream fis = new FileInputStream(this.file);
		System.out.print("file "+file);
		XSSFWorkbook workbook = new XSSFWorkbook(fis);

		XSSFSheet sheet = workbook.getSheetAt(0);

		Iterator<Row> rowIt = sheet.iterator();
		int i=0;
		while (rowIt.hasNext()) {
			Row row = rowIt.next();
			//row.getCell(3);
			if(row.getCell(3)!=null){
				if(row.getCell(3).getCellType()==Cell.CELL_TYPE_NUMERIC){
//					System.out.println(row.getCell(3).getNumericCellValue());
					concentration.add((Double)row.getCell(3).getNumericCellValue());
					i++;
				}
			}
			
			// iterate on cells for the current row
//			Iterator<Cell> cellIterator = row.cellIterator();
//
//			while (cellIterator.hasNext()) {
//				Cell cell = cellIterator.next();
//
//
//				if(cell.getCellType()==Cell.CELL_TYPE_NUMERIC)
//				{
//					System.out.println(cell.getNumericCellValue());
//
//				}else {
//					System.out.println(cell.getStringCellValue());
//				}
//			}
//
		}
		workbook.close();
		fis.close();
	}

	public List<Double> getConcentration() {
		return concentration;
	}
}
