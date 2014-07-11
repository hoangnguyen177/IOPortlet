com_example_testparallelcoordinates3_ParCoords = function() {
	var e = $(this.getElement());
	var parcoorContext = e.context;
	
	if(parcoorContext.getAttribute('initialized')===null || parcoorContext.getAttribute('initialized')===false){
		try{  
			 var parcoordDiv=document.createElement('div'); 
			 parcoordDiv.setAttribute('id', "example");
			 parcoordDiv.setAttribute('class', "parcoords");
			 parcoordDiv.setAttribute('style', "height:240px;");
			 parcoorContext.appendChild(parcoordDiv); 
			 var gridDiv=document.createElement('div'); 
			 gridDiv.setAttribute('id', "grid");
			 parcoorContext.appendChild(gridDiv); 
			 var pagerDiv=document.createElement('div'); 
			 pagerDiv.setAttribute('id', "pager");
			 parcoorContext.appendChild(pagerDiv); 
			 
	    }catch(e) 
	    {window.alert(e);};
	     
	    var parcoords = d3.parcoords()("#example")
						.alpha(this.getState().alpha)
						.mode(this.getState().mode)
						.width(this.getState().pcWidth)
						.height(this.getState().pcHeight/2)
						.shadows()
						.autoscale()
						.reorderable()
						.mode("queue")
						;
	    parcoorContext.setAttribute('initialized', 'true');
	    	    
	    var dataView = new Slick.Data.DataView();
	    
	}
	
	function gridUpdate(data) {
	    dataView.beginUpdate();
	    dataView.setItems(data);
	    dataView.endUpdate();
	};
	
	
		
	  
    var self = this;
    parcoords.on("brush", function(data){
    	gridUpdate(data);
    });
    
    parcoords.on("brush_range", function(data){
    	self.setBrushRange(JSON.stringify(data));
    });
    
    
    
    var first = true;
    this.onStateChange = function() {
		//data
	  	if(typeof this.getState().data!== 'undefined' && this.getState().data.length > 0){
	  		parcoords.data(this.getState().data);
	  	}
	  	//dimension
	  	if(typeof this.getState().dimensions!== 'undefined' && this.getState().dimensions.length > 0){
	  		parcoords.dimensions(this.getState().dimensions);
	  	}
	  	//types
	  	if(typeof this.getState().types!== 'undefined' && this.getState().types.length > 0){
	  		parcoords.types(this.getState().types);
	  	}
	  	//margin
	  	if(typeof this.getState().margin!== 'undefined' && this.getState().margin.length > 0){
	  		parcoords.margin(this.getState().margin);
	  	}
//	  	canvas.setAttribute("style", canvas.getAttribute("style") + " opacity: 0.1;");
	  	if(first=== true){
		  	if(this.getState().brushed==true)
		  		parcoords.render().createAxes().brushable();
		  	else
		  		parcoords.render().createAxes();
		  	first = false;
		  	
		  	//grid
		  	var column_keys = parcoords.dimensions();
		    var columns = column_keys.map(function(key,i) {
		      return {
		        id: key,
		        name: key,
		        field: key,
		        sortable: true
		      }
		    });
		    
		    var options = {
		      enableCellNavigation: true,
		      enableColumnReorder: false,
		      multiColumnSort: false
		    };		    
		    var grid = new Slick.Grid("#grid", dataView, columns, options);
		    var pager = new Slick.Controls.Pager(dataView, grid, $("#pager"));
		    // wire up model events to drive the grid
		    dataView.onRowCountChanged.subscribe(function (e, args) {
		      grid.updateRowCount();
		      grid.render();
		    });
		    dataView.onRowsChanged.subscribe(function (e, args) {
		        grid.invalidateRows(args.rows);
			    grid.render();
		    });
		    
		    // column sorting
		    var sortcol = column_keys[0];
		    var sortdir = 1;

		    function comparer(a, b) {
		      var x = a[sortcol], y = b[sortcol];
		      return (x == y ? 0 : (x > y ? 1 : -1));
		    }
		    
		    // click header to sort grid column
		    grid.onSort.subscribe(function (e, args) {
		      sortdir = args.sortAsc ? 1 : -1;
		      sortcol = args.sortCol.field;

		      if ($.browser.msie && $.browser.version <= 8) {
		        dataView.fastSort(sortcol, args.sortAsc);
		      } else {
		        dataView.sort(comparer, args.sortAsc);
		      }
		    });
		    
		    // highlight row in chart
		    grid.onMouseEnter.subscribe(function(e,args) {
		      var i = grid.getCellFromEvent(e).row;
		      var d = parcoords.brushed() || parcoords.data();
		      parcoords.highlight([d[i]]);
		    });
		    //clear the highlight
		    grid.onMouseLeave.subscribe(function(e,args) {
		      parcoords.unhighlight();
		    });
		    
		    grid.onClick.subscribe(function (e) {
		    	var i = grid.getCellFromEvent(e).row;
		    	var d = parcoords.brushed() || parcoords.data();
			    //console.log(d[i]);
		    	//create new window here
		    	self.onRowSelected(d[i]);
		    });
		    
		    
		    
	  	}
	  	else{
	  		parcoords.render().updateAxes;
		  	if(this.getState().autoscale == true){
		  		parcoords.resize();
		  	}
		}
	  	
	  	if(parcoords.brushed() == false || parcoords.brushed().length == 0)
	  		gridUpdate(this.getState().data);
	  	
	  	
	  	
  		
	}
      

      
      
      
};