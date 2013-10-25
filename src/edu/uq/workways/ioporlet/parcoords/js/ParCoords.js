edu_uq_workways_ioporlet_parcoords_ParCoords = function() {
	var e = this.getElement();
    var parcoords = d3.parcoords()("#"+ this.getState().containerId)
					.alpha(this.getState().alpha)
					.mode(this.getState().mode)
					.width(this.getState().pcWidth)
					.height(this.getState().pcHeight)
					.shadows()
					.autoscale()
					.mode("queue")
					.brushable()
					;
    //first time paint
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
	  	if(first=== true){
            parcoords.render().createAxes();
            if(this.getState().brushed==true)
            	parcoords.brushable();
            first = false;
	    }
	    else{
            parcoords.render().updateAxes();
	    }
	}
      
};